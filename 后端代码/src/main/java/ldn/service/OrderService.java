package ldn.service;

import ldn.error.BussinessException;
import ldn.service.model.OrderModel;

public interface OrderService {
    OrderModel createOrder(Integer userId, Integer itemId,Integer promoId, Integer amount) throws BussinessException;

}
