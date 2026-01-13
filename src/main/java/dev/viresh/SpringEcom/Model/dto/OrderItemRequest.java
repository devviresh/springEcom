package dev.viresh.SpringEcom.Model.dto;

public record OrderItemRequest (
    int productId,
    int quantity
){}
