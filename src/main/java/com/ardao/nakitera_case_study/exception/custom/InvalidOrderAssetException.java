package com.ardao.nakitera_case_study.exception.custom;

import lombok.Getter;

@Getter
public class InvalidOrderAssetException extends RuntimeException {
  private final Object[] args;

  public InvalidOrderAssetException(String message, Object... args) {
    super(message);
    this.args = args;
  }

}
