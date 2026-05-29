package com.ardao.nakitera_case_study.unittests;

import com.ardao.nakitera_case_study.entity.Asset;
import com.ardao.nakitera_case_study.entity.Customer;
import com.ardao.nakitera_case_study.entity.Order;
import com.ardao.nakitera_case_study.entity.box.OrderInbox;
import com.ardao.nakitera_case_study.entity.box.OrderOutbox;
import com.ardao.nakitera_case_study.enums.Side;
import com.ardao.nakitera_case_study.event.dto.OrderEvent;
import com.ardao.nakitera_case_study.repository.AssetRepository;
import com.ardao.nakitera_case_study.repository.CustomerRepository;
import com.ardao.nakitera_case_study.repository.box.MatchedOrderInboxRepository;
import com.ardao.nakitera_case_study.repository.box.OrderInboxRepository;
import com.ardao.nakitera_case_study.repository.box.OrderOutboxRepository;
import com.ardao.nakitera_case_study.response.AssetListResponse;
import com.ardao.nakitera_case_study.service.impl.AssetServiceImpl;
import com.ardao.nakitera_case_study.util.resolver.CustomerIdResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AssetServiceUnitTest {
    @Mock
    private OrderOutboxRepository orderOutboxRepository;

    @Mock
    private OrderInboxRepository orderInboxRepository;

    @Mock
    private MatchedOrderInboxRepository matchedOrderInboxRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerIdResolver customerIdResolver;

    @InjectMocks
    private AssetServiceImpl assetService;


    @Test
    void handleOrderCreatedBuyEvent_shouldReserveTryWhenBalanceIsEnough() {
        Asset tryAsset = new Asset();
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(5000);
        tryAsset.setUsableSize(5000);


        OrderEvent orderEvent = new OrderEvent(
                1L,
                10L,
                2L,
                "THY",
                Side.BUY,
                5,
                100,
                "ORDER_CREATED"
        );

        when(orderInboxRepository.existsByOutboxId(1L)).thenReturn(false);
        when(assetRepository.getAssetByAssetNameAndCustomer_Id("TRY", 2L))
                .thenReturn(Optional.of(tryAsset));

        assetService.handleOrderCreatedBuyEvent(orderEvent);

        assertEquals(4500, tryAsset.getUsableSize());

        ArgumentCaptor<OrderInbox> inboxCaptor = ArgumentCaptor.forClass(OrderInbox.class);
        verify(orderInboxRepository).save(inboxCaptor.capture());

        OrderInbox orderInbox = inboxCaptor.getValue();
        assertEquals(1L, orderInbox.getOutboxId());
        assertEquals(10L, orderInbox.getOrderId());
    }

    @Test
    void handleOrderCreatedBuyEvent_shouldCreateFailureEventWhenTryAssetIsMissing() {
        OrderEvent orderEvent = new OrderEvent(
                1L,
                10L,
                2L,
                "THY",
                Side.BUY,
                5,
                100,
                "ORDER_CREATED"
        );

        when(orderInboxRepository.existsByOutboxId(1L)).thenReturn(false);
        when(assetRepository.getAssetByAssetNameAndCustomer_Id("TRY", 2L))
                .thenReturn(Optional.empty());

        assetService.handleOrderCreatedBuyEvent(orderEvent);

        ArgumentCaptor<OrderOutbox> outboxCaptor = ArgumentCaptor.forClass(OrderOutbox.class);
        verify(orderOutboxRepository).save(outboxCaptor.capture());

        OrderOutbox savedOutbox = outboxCaptor.getValue();
        assertEquals("ASSET_RESERVATION_FAILED", savedOutbox.getEventType());
        assertEquals(orderEvent.orderId(), savedOutbox.getOrderId());
        assertEquals(orderEvent.customerId(), savedOutbox.getCustomerId());
        assertEquals(orderEvent.assetName(), savedOutbox.getAssetName());

    }

    @Test
    void handleOrderCreatedBuyEvent_shouldCreateFailureEventWhenTryBalanceIsInsufficient() {
        Asset tryAsset = new Asset();
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(100);
        tryAsset.setUsableSize(100);

        OrderEvent orderEvent = new OrderEvent(
                1L,
                10L,
                2L,
                "THY",
                Side.BUY,
                5,
                100,
                "ORDER_CREATED"
        );

        when(orderInboxRepository.existsByOutboxId(1L)).thenReturn(false);
        when(assetRepository.getAssetByAssetNameAndCustomer_Id("TRY", 2L))
                .thenReturn(Optional.of(tryAsset));

        assetService.handleOrderCreatedBuyEvent(orderEvent);

        assertEquals(100, tryAsset.getUsableSize());

        ArgumentCaptor<OrderOutbox> outboxCaptor = ArgumentCaptor.forClass(OrderOutbox.class);
        verify(orderOutboxRepository).save(outboxCaptor.capture());

        OrderOutbox savedOutbox = outboxCaptor.getValue();
        assertEquals("ASSET_RESERVATION_FAILED", savedOutbox.getEventType());
        assertEquals(orderEvent.orderId(), savedOutbox.getOrderId());
        assertEquals(orderEvent.customerId(), savedOutbox.getCustomerId());
    }

    @Test
    void handleOrderCreatedBuyEvent_shouldSkipWhenEventAlreadyProcessed() {
        OrderEvent orderEvent = new OrderEvent(
                1L,
                10L,
                2L,
                "THY",
                Side.BUY,
                5,
                100,
                "ORDER_CREATED"
        );

        when(orderInboxRepository.existsByOutboxId(1L)).thenReturn(true);

        assetService.handleOrderCreatedBuyEvent(orderEvent);

        verify(assetRepository, never()).getAssetByAssetNameAndCustomer_Id(anyString(), anyLong());
        verify(orderOutboxRepository, never()).save(any());
    }

    @Test
    void handleOrderCreatedSellEvent_shouldReserveAssetWhenUsableSizeIsEnough() {
        Asset stockAsset = new Asset();
        stockAsset.setAssetName("THY");
        stockAsset.setSize(10);
        stockAsset.setUsableSize(10);

        OrderEvent orderEvent = new OrderEvent(
                1L,
                10L,
                2L,
                "THY",
                Side.SELL,
                5,
                100,
                "ORDER_CREATED"
        );
        when(orderInboxRepository.existsByOutboxId(1L)).thenReturn(false);
        when(assetRepository.getAssetByAssetNameAndCustomer_Id("THY", 2L))
                .thenReturn(Optional.of(stockAsset));

        assetService.handleOrderCreatedSellEvent(orderEvent);
        assertEquals(5, stockAsset.getUsableSize());

        ArgumentCaptor<OrderInbox> orderInboxArgumentCaptor = ArgumentCaptor.forClass(OrderInbox.class);
        verify(orderInboxRepository).save(orderInboxArgumentCaptor.capture());

        OrderInbox savedInbox = orderInboxArgumentCaptor.getValue();
        assertEquals(orderEvent.outboxId(), savedInbox.getOutboxId());
        assertEquals(orderEvent.orderId(), savedInbox.getOrderId());

    }
    @Test
    void handleOrderCreatedSellEvent_shouldCreateFailureEventWhenAssetIsMissing() {
        OrderEvent orderEvent = new OrderEvent(
                1L,
                10L,
                2L,
                "THY",
                Side.SELL,
                5,
                100,
                "ORDER_CREATED"
        );
        when(orderInboxRepository.existsByOutboxId(1L)).thenReturn(false);
        when(assetRepository.getAssetByAssetNameAndCustomer_Id("THY",2L))
                                                            .thenReturn(Optional.empty());

        assetService.handleOrderCreatedSellEvent(orderEvent);

        ArgumentCaptor<OrderOutbox> argumentCaptor = ArgumentCaptor.forClass(OrderOutbox.class);
        verify(orderOutboxRepository).save(argumentCaptor.capture());

        OrderOutbox outbox = argumentCaptor.getValue();
        assertEquals("ASSET_RESERVATION_FAILED", outbox.getEventType());

    }

    @Test
    void handleOrderCreatedSellEvent_shouldCreateFailureEventWhenUsableSizeIsInsufficient() {
        Asset stockAsset = new Asset();
        stockAsset.setAssetName("THY");
        stockAsset.setSize(3);
        stockAsset.setUsableSize(3);

        OrderEvent orderEvent = new OrderEvent(
                1L,
                10L,
                2L,
                "THY",
                Side.SELL,
                5,
                100,
                "ORDER_CREATED"
        );

        when(orderInboxRepository.existsByOutboxId(1L)).thenReturn(false);
        when(assetRepository.getAssetByAssetNameAndCustomer_Id("THY", 2L))
                .thenReturn(Optional.of(stockAsset));

        assetService.handleOrderCreatedSellEvent(orderEvent);

        assertEquals(3, stockAsset.getUsableSize());

        ArgumentCaptor<OrderOutbox> outboxCaptor = ArgumentCaptor.forClass(OrderOutbox.class);
        verify(orderOutboxRepository).save(outboxCaptor.capture());

        OrderOutbox savedOutbox = outboxCaptor.getValue();
        assertEquals("ASSET_RESERVATION_FAILED", savedOutbox.getEventType());
        assertEquals(orderEvent.orderId(), savedOutbox.getOrderId());
        assertEquals(orderEvent.customerId(), savedOutbox.getCustomerId());

        ArgumentCaptor<OrderInbox> inboxCaptor = ArgumentCaptor.forClass(OrderInbox.class);
        verify(orderInboxRepository).save(inboxCaptor.capture());

        OrderInbox savedInbox = inboxCaptor.getValue();
        assertEquals(orderEvent.outboxId(), savedInbox.getOutboxId());
        assertEquals(orderEvent.orderId(), savedInbox.getOrderId());
    }

    @Test
    void handleOrderCreatedSellEvent_shouldSkipWhenEventAlreadyProcessed() {
        OrderEvent orderEvent = new OrderEvent(
                1L,
                10L,
                2L,
                "THY",
                Side.SELL,
                5,
                100,
                "ORDER_CREATED"
        );

        when(orderInboxRepository.existsByOutboxId(1L)).thenReturn(true);

        assetService.handleOrderCreatedSellEvent(orderEvent);

        verify(assetRepository, never()).getAssetByAssetNameAndCustomer_Id(anyString(), anyLong());
        verify(orderOutboxRepository, never()).save(any());
        verify(orderInboxRepository, never()).save(any());
    }

    @Test
    void releaseReservedBuyOrder_shouldRestoreUsableTry() {
        Asset tryAsset = new Asset();
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(5000);
        tryAsset.setUsableSize(4500);

        OrderEvent orderEvent = new OrderEvent(
                1L,
                10L,
                2L,
                "THY",
                Side.BUY,
                5,
                100,
                "ORDER_CANCELED"
        );

        when(orderInboxRepository.existsByOutboxId(1L)).thenReturn(false);
        when(assetRepository.getAssetByAssetNameAndCustomer_Id("TRY", 2L))
                .thenReturn(Optional.of(tryAsset));

        assetService.releaseReservedBuyOrder(orderEvent);

        assertEquals(5000, tryAsset.getUsableSize());

        ArgumentCaptor<OrderInbox> inboxCaptor = ArgumentCaptor.forClass(OrderInbox.class);
        verify(orderInboxRepository).save(inboxCaptor.capture());

        OrderInbox savedInbox = inboxCaptor.getValue();
        assertEquals(1L, savedInbox.getOutboxId());
        assertEquals(10L, savedInbox.getOrderId());
    }
    @Test
    void releaseReservedSellOrder_shouldRestoreUsableAsset() {
        Asset stockAsset = new Asset();
        stockAsset.setAssetName("THY");
        stockAsset.setSize(10);
        stockAsset.setUsableSize(5);

        OrderEvent orderEvent = new OrderEvent(
                1L,
                10L,
                2L,
                "THY",
                Side.SELL,
                5,
                100,
                "ORDER_CANCELED"
        );

        when(orderInboxRepository.existsByOutboxId(1L)).thenReturn(false);
        when(assetRepository.getAssetByAssetNameAndCustomer_Id("THY", 2L))
                .thenReturn(Optional.of(stockAsset));

        assetService.releaseReservedSellOrder(orderEvent);

        assertEquals(10, stockAsset.getUsableSize());

        ArgumentCaptor<OrderInbox> inboxCaptor = ArgumentCaptor.forClass(OrderInbox.class);
        verify(orderInboxRepository).save(inboxCaptor.capture());

        OrderInbox savedInbox = inboxCaptor.getValue();
        assertEquals(1L, savedInbox.getOutboxId());
        assertEquals(10L, savedInbox.getOrderId());
    }
    @Test
    void getCustomerAssetList_shouldReturnMappedPage() {
        Customer customer = new Customer();
        customer.setId(2L);

        Asset asset = new Asset();
        asset.setId(10L);
        asset.setCustomer(customer);
        asset.setAssetName("THY");
        asset.setSize(5);
        asset.setUsableSize(5);

        Page<Asset> assetPage = new PageImpl<>(List.of(asset));
        when(customerIdResolver.resolveCustomerId(2L)).thenReturn(2L);
        when(assetRepository.getAssetByCustomer_Id(eq(2L), any(PageRequest.class)))
                .thenReturn(assetPage);

        Page<AssetListResponse> result = assetService.getCustomerAssetList(0, 10, 2L);

        assertEquals("THY", result.getContent().getFirst().assetName());

    }
}
