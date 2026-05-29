package com.ardao.nakitera_case_study.service.impl;

import com.ardao.nakitera_case_study.entity.Asset;
import com.ardao.nakitera_case_study.entity.Customer;
import com.ardao.nakitera_case_study.entity.box.MatchedOrderInbox;
import com.ardao.nakitera_case_study.entity.box.OrderInbox;
import com.ardao.nakitera_case_study.entity.box.OrderOutbox;
import com.ardao.nakitera_case_study.event.dto.MatchedOrderEvent;
import com.ardao.nakitera_case_study.event.dto.OrderEvent;
import com.ardao.nakitera_case_study.exception.custom.CustomerNotFoundException;
import com.ardao.nakitera_case_study.repository.AssetRepository;
import com.ardao.nakitera_case_study.repository.CustomerRepository;
import com.ardao.nakitera_case_study.repository.box.MatchedOrderInboxRepository;
import com.ardao.nakitera_case_study.repository.box.OrderInboxRepository;
import com.ardao.nakitera_case_study.repository.box.OrderOutboxRepository;
import com.ardao.nakitera_case_study.response.AssetListResponse;
import com.ardao.nakitera_case_study.service.AssetService;
import com.ardao.nakitera_case_study.util.mapper.asset.AssetMapper;
import com.ardao.nakitera_case_study.util.resolver.CustomerIdResolver;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class AssetServiceImpl implements AssetService {

    private final OrderOutboxRepository orderOutboxRepository;
    private final OrderInboxRepository orderInboxRepository;
    private final MatchedOrderInboxRepository matchedOrderInboxRepository;
    private final AssetRepository assetRepository;
    private final CustomerRepository customerRepository;
    private final CustomerIdResolver customerIdResolver;

    public AssetServiceImpl(OrderOutboxRepository orderOutboxRepository, OrderInboxRepository orderInboxRepository, MatchedOrderInboxRepository matchedOrderInboxRepository, AssetRepository assetRepository, CustomerRepository customerRepository, CustomerIdResolver customerIdResolver) {
        this.orderOutboxRepository = orderOutboxRepository;
        this.orderInboxRepository = orderInboxRepository;
        this.matchedOrderInboxRepository = matchedOrderInboxRepository;
        this.assetRepository = assetRepository;
        this.customerRepository = customerRepository;
        this.customerIdResolver = customerIdResolver;
    }

    @Override
    @Transactional
    public void handleOrderCreatedBuyEvent(OrderEvent orderEvent) {
        if (isEventProcessed(orderEvent.outboxId())) {
            log.warn("BUY reservation skipped because event already processed. outboxId={}, orderId={}",
                    orderEvent.outboxId(), orderEvent.orderId());

            return;
        }
        log.info("Processing BUY reservation. outboxId={}, orderId={}, customerId={}, assetName={}, size={}, price={}",
                orderEvent.outboxId(), orderEvent.orderId(), orderEvent.customerId(),
                orderEvent.assetName(), orderEvent.size(), orderEvent.price());

        Optional<Asset> assetOptional = getTRYAsset(orderEvent.customerId());

        if (assetOptional.isEmpty()) {
            createAssetReservationFailedEvent(orderEvent);
            log.warn("BUY reservation failed: TRY asset not found. outboxId={}, orderId={}, customerId={}",
                    orderEvent.outboxId(), orderEvent.orderId(), orderEvent.customerId());

            return;
        }

        Asset asset = assetOptional.get();
        int newUsableTRYSize = asset.getUsableSize() - (orderEvent.size() * orderEvent.price());

        if (newUsableTRYSize < 0) {
            createAssetReservationFailedEvent(orderEvent);
            log.warn("BUY reservation failed: insufficient usable TRY. outboxId={}, orderId={}, customerId={}, required={}, usable={}",
                    orderEvent.outboxId(), orderEvent.orderId(), orderEvent.customerId(),
                    orderEvent.size() * orderEvent.price(), asset.getUsableSize());
            return;
        }

        asset.setUsableSize(newUsableTRYSize);

        OrderInbox newOrderInbox = new OrderInbox();
        newOrderInbox.setOutboxId(orderEvent.outboxId());
        newOrderInbox.setOrderId(orderEvent.orderId());

        this.orderInboxRepository.save(newOrderInbox);

        log.info("BUY reservation completed. outboxId={}, orderId={}, customerId={}, newUsableTry={}",
                orderEvent.outboxId(), orderEvent.orderId(), orderEvent.customerId(), asset.getUsableSize());
    }

    @Override
    @Transactional
    public void handleOrderCreatedSellEvent(OrderEvent orderEvent) {
        if (isEventProcessed(orderEvent.outboxId())) {
            log.warn("SELL reservation skipped because event already processed. outboxId={}, orderId={}",
                    orderEvent.outboxId(), orderEvent.orderId());
            return;
        }

        log.info("Processing SELL reservation. outboxId={}, orderId={}, customerId={}, assetName={}, size={}, price={}",
                orderEvent.outboxId(), orderEvent.orderId(), orderEvent.customerId(),
                orderEvent.assetName(), orderEvent.size(), orderEvent.price());

        Optional<Asset> assetOptional = getCustomAsset(orderEvent.assetName(), orderEvent.customerId());

        if (assetOptional.isEmpty()) {
            createAssetReservationFailedEvent(orderEvent);
            log.warn("SELL reservation failed: asset not found. outboxId={}, orderId={}, customerId={}, assetName={}",
                    orderEvent.outboxId(), orderEvent.orderId(), orderEvent.customerId(), orderEvent.assetName());
            return;
        }

        Asset asset = assetOptional.get();
        int newUsableCustomAssetSize = asset.getUsableSize() - orderEvent.size();

        if (newUsableCustomAssetSize < 0) {
            createAssetReservationFailedEvent(orderEvent);
            log.warn("SELL reservation failed: insufficient usable asset. outboxId={}, orderId={}, customerId={}, assetName={}, requestedSize={}, usableSize={}",
                    orderEvent.outboxId(), orderEvent.orderId(), orderEvent.customerId(),
                    orderEvent.assetName(), orderEvent.size(), asset.getUsableSize());
            return;
        }

        asset.setUsableSize(newUsableCustomAssetSize);

        OrderInbox newOrderInbox = new OrderInbox();
        newOrderInbox.setOutboxId(orderEvent.outboxId());
        newOrderInbox.setOrderId(orderEvent.orderId());

        this.orderInboxRepository.save(newOrderInbox);

        log.info("SELL reservation completed. outboxId={}, orderId={}, customerId={}, assetName={}, newUsableSize={}",
                orderEvent.outboxId(), orderEvent.orderId(), orderEvent.customerId(),
                orderEvent.assetName(), newUsableCustomAssetSize);
    }

    @Override
    @Transactional
    public void handleMatchedOrderEvent(MatchedOrderEvent matchedOrderEvent) {
        if(isMatchedEventProcessed(matchedOrderEvent.outboxId())){
            log.warn("Matched order event already processed. outboxId={}", matchedOrderEvent.outboxId());
            return;
        }
        log.info("Processing matched order event. outboxId={}", matchedOrderEvent.outboxId());

        Optional<Asset> buyerTryAssetOptional = getTRYAsset(matchedOrderEvent.buyCustomerId());
        Optional<Asset> sellerTryAssetOptional = getTRYAsset(matchedOrderEvent.sellCustomerId());
        Optional<Asset> sellerStockAssetOptional = getCustomAsset(
                matchedOrderEvent.assetName(),
                matchedOrderEvent.sellCustomerId()
        );

        if (buyerTryAssetOptional.isEmpty() || sellerTryAssetOptional.isEmpty() || sellerStockAssetOptional.isEmpty()) {
            log.warn("Matched order settlement skipped due to missing asset. outboxId={}", matchedOrderEvent.outboxId());
            return;
        }


          Asset buyerTryAsset = buyerTryAssetOptional.get();
          Asset sellerTryAsset = sellerTryAssetOptional.get();
          Asset sellerStockAsset = sellerStockAssetOptional.get();

       Asset buyerStockAsset = getCustomAsset(matchedOrderEvent.assetName(), matchedOrderEvent.buyCustomerId())
               .orElseGet(()-> createCustomerAsset(matchedOrderEvent.buyCustomerId(), matchedOrderEvent.assetName()));

        int totalPriceAmount = matchedOrderEvent.sellPrice() * matchedOrderEvent.matchedSize();
        int reservedAmount = matchedOrderEvent.buyPrice() * matchedOrderEvent.matchedSize();
        int refundAmount = reservedAmount - totalPriceAmount;

        buyerTryAsset.setSize(buyerTryAsset.getSize() - totalPriceAmount);
        buyerTryAsset.setUsableSize(buyerTryAsset.getUsableSize() + refundAmount);

        buyerStockAsset.setSize(buyerStockAsset.getSize() + matchedOrderEvent.matchedSize());
        buyerStockAsset.setUsableSize(buyerStockAsset.getUsableSize() + matchedOrderEvent.matchedSize());

        sellerStockAsset.setSize(sellerStockAsset.getSize() - matchedOrderEvent.matchedSize());

        sellerTryAsset.setSize(sellerTryAsset.getSize() + totalPriceAmount);
        sellerTryAsset.setUsableSize(sellerTryAsset.getUsableSize() + totalPriceAmount);

        this.assetRepository.save(buyerStockAsset);

        MatchedOrderInbox matchedOrderInbox = new MatchedOrderInbox();
        matchedOrderInbox.setMatchedOutboxId(matchedOrderEvent.outboxId());
        matchedOrderInbox.setBuyOrderId(matchedOrderEvent.buyOrderId());
        matchedOrderInbox.setSellOrderId(matchedOrderEvent.sellOrderId());

        this.matchedOrderInboxRepository.save(matchedOrderInbox);
        log.info("Matched order settled.");
    }

    @Override
    @Transactional
    public void releaseReservedBuyOrder(OrderEvent orderEvent) {
        if (isEventProcessed(orderEvent.outboxId())) {
            log.warn("BUY release skipped because event already processed. outboxId={}, orderId={}",
                    orderEvent.outboxId(), orderEvent.orderId());
            return;
        }

        log.info("Releasing BUY reservation. outboxId={}, orderId={}, customerId={}, size={}, price={}",
                orderEvent.outboxId(), orderEvent.orderId(), orderEvent.customerId(),
                orderEvent.size(), orderEvent.price());

        Optional<Asset> assetOptional = getTRYAsset(orderEvent.customerId());

        if (assetOptional.isEmpty()) {
            log.warn("BUY release skipped because TRY asset not found. outboxId={}, orderId={}, customerId={}",
                    orderEvent.outboxId(), orderEvent.orderId(), orderEvent.customerId());
            return;
        }

        Asset asset = assetOptional.get();
        int newUsableSize = asset.getUsableSize() + (orderEvent.price() * orderEvent.size());
        asset.setUsableSize(newUsableSize);

        OrderInbox newOrderInbox = new OrderInbox();
        newOrderInbox.setOutboxId(orderEvent.outboxId());
        newOrderInbox.setOrderId(orderEvent.orderId());

        this.orderInboxRepository.save(newOrderInbox);

        log.info("BUY reservation released. outboxId={}, orderId={}, customerId={}, newUsableTry={}",
                orderEvent.outboxId(), orderEvent.orderId(), orderEvent.customerId(), newUsableSize);
    }

    @Override
    @Transactional
    public void releaseReservedSellOrder(OrderEvent orderEvent) {
        if (isEventProcessed(orderEvent.outboxId())) {
            log.warn("SELL release skipped because event already processed. outboxId={}, orderId={}",
                    orderEvent.outboxId(), orderEvent.orderId());
            return;
        }

        log.info("Releasing SELL reservation. outboxId={}, orderId={}, customerId={}, assetName={}, size={}",
                orderEvent.outboxId(), orderEvent.orderId(), orderEvent.customerId(),
                orderEvent.assetName(), orderEvent.size());

        Optional<Asset> assetOptional = getCustomAsset(orderEvent.assetName(), orderEvent.customerId());

        if (assetOptional.isEmpty()) {
            log.warn("SELL release skipped because asset not found. outboxId={}, orderId={}, customerId={}, assetName={}",
                    orderEvent.outboxId(), orderEvent.orderId(), orderEvent.customerId(), orderEvent.assetName());
            return;
        }

        Asset asset = assetOptional.get();
        int newUsableSize = asset.getUsableSize() + orderEvent.size();
        asset.setUsableSize(newUsableSize);

        OrderInbox newOrderInbox = new OrderInbox();
        newOrderInbox.setOutboxId(orderEvent.outboxId());
        newOrderInbox.setOrderId(orderEvent.orderId());

        this.orderInboxRepository.save(newOrderInbox);

        log.info("SELL reservation released. outboxId={}, orderId={}, customerId={}, assetName={}, newUsableSize={}",
                orderEvent.outboxId(), orderEvent.orderId(), orderEvent.customerId(),
                orderEvent.assetName(), newUsableSize);
    }

    @Override
    public Page<AssetListResponse> getCustomerAssetList(int page, int size, Long customerId) {
        long id = this.customerIdResolver.resolveCustomerId(customerId);
        log.info("Getting asset list of customer  id: {}.",id);
        PageRequest pageRequest = PageRequest.of(page,size);
        return this.assetRepository.getAssetByCustomer_Id(id,pageRequest).map(AssetMapper::toAssetListResponse);
    }




    private void createAssetReservationFailedEvent(OrderEvent orderEvent){
        log.warn("Creating ASSET_RESERVATION_FAILED event.");
        OrderOutbox AssetReservationFailedEvent = new OrderOutbox();

        AssetReservationFailedEvent.setAssetName(orderEvent.assetName());
        AssetReservationFailedEvent.setCustomerId(orderEvent.customerId());
        AssetReservationFailedEvent.setEventType("ASSET_RESERVATION_FAILED");
        AssetReservationFailedEvent.setSize(orderEvent.size());
        AssetReservationFailedEvent.setPrice(orderEvent.price());
        AssetReservationFailedEvent.setOrderSide(orderEvent.orderSide());
        AssetReservationFailedEvent.setOrderId(orderEvent.orderId());

        orderOutboxRepository.save(AssetReservationFailedEvent);

        OrderInbox newOrderInbox = new OrderInbox();
        newOrderInbox.setOutboxId(orderEvent.outboxId());
        newOrderInbox.setOrderId(orderEvent.orderId());

        this.orderInboxRepository.save(newOrderInbox);
        log.info("ASSET_RESERVATION_FAILED event created. sourceOutboxId={}, failedOrderId={}, newOutboxEventType={}",
                orderEvent.outboxId(), orderEvent.orderId(), "ASSET_RESERVATION_FAILED");
    }

    private boolean isEventProcessed(Long outboxId){
        boolean isEventProcessed = this.orderInboxRepository.existsByOutboxId(outboxId);
        return isEventProcessed;
    }

    private boolean isMatchedEventProcessed(Long matchedOutboxId){
        boolean isEventProcessed = this.matchedOrderInboxRepository.existsByMatchedOutboxId(matchedOutboxId);
        return isEventProcessed;
    }
    public Asset createTRYAsset(Customer customer) {
        int usableSize = 5000;
        Asset newAsset = new Asset();
        newAsset.setAssetName("TRY");
        newAsset.setSize(usableSize);
        newAsset.setCustomer(customer);
        newAsset.setUsableSize(usableSize);
        log.info("TRY asset initialized for customer. customerId={}, size={}, usableSize={}",
                customer.getId(), newAsset.getSize(), newAsset.getUsableSize());
        return newAsset;
    }

    private Optional<Asset> getTRYAsset(Long customerId) {
        return this.assetRepository.getAssetByAssetNameAndCustomer_Id("TRY", customerId);
    }

    private Optional<Asset> getCustomAsset(String assetName, Long customerId) {
        return this.assetRepository.getAssetByAssetNameAndCustomer_Id(
                assetName,
                customerId
        );
    }


    private Asset createCustomerAsset(Long customerId, String assetName) {
        Asset newAsset = new Asset();
        newAsset.setCustomer(this.customerRepository.findById(customerId)
                        .orElseThrow(() -> new CustomerNotFoundException("customer.not.found.exception", customerId))
        );
        newAsset.setAssetName(assetName);
        newAsset.setSize(0);
        newAsset.setUsableSize(0);
        log.info("Creating new customer asset. customerId={}, assetName={}", customerId, assetName);
        return newAsset;
    }



}
