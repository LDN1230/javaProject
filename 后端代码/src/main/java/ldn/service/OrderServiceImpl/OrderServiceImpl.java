package ldn.service.OrderServiceImpl;

import ldn.DataObject.OrderDO;
import ldn.DataObject.SequenceDO;
import ldn.dao.ItemStockDOMapper;
import ldn.dao.OrderDOMapper;
import ldn.dao.SequenceDOMapper;
import ldn.error.BussinessException;
import ldn.error.EmBussinessError;
import ldn.service.ItemService;
import ldn.service.OrderService;
import ldn.service.UserService;
import ldn.service.model.ItemModel;
import ldn.service.model.OrderModel;
import ldn.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemStockDOMapper itemStockDOMapper;
    @Autowired
    private OrderDOMapper orderDOMapper;
    @Autowired
    private SequenceDOMapper sequenceDOMapper;
    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BussinessException {
        //1.校验下单状态，下单的商品时候存在，用户是否合法，购买数量是否正确
        ItemModel itemModel = itemService.getItemById(itemId);
        if(itemModel == null)
            throw new BussinessException(EmBussinessError.PARANETDER_VALIDATION_ERROR, "商品信息不存在");
        UserModel userModel = userService.getUserId(userId);
        if(userModel == null)
            throw new BussinessException(EmBussinessError.PARANETDER_VALIDATION_ERROR, "用户不存在");
        if(amount <= 0 || amount >=99)
            throw new BussinessException(EmBussinessError.PARANETDER_VALIDATION_ERROR, "数量信息不准确");

        //检验活动信息
        if(promoId != null)
        {
            //(1)检验对应活动是否存在适用的商品
            if(promoId != itemModel.getPromoModel().getId())
            {
                throw new BussinessException(EmBussinessError.PARANETDER_VALIDATION_ERROR,"活动信息不正确");
            //(2）检验活动是否正在进行中
            }else if(itemModel.getPromoModel().getStatus() != 2){
                throw new BussinessException(EmBussinessError.PARANETDER_VALIDATION_ERROR,"活动还未开始");
            }
        }
        //2.落单减库存 支付减库存
        boolean result = itemService.decreaseStock(itemId, amount);
        if(!result)
            throw new BussinessException(EmBussinessError.STOCK_NOT_ENOUGH);
        //3.订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        if(promoId != null)
        {
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else{
            orderModel.setItemPrice(itemModel.getPrice());
        }

        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));
        //生成交易流水号，订单号
        orderModel.setId(generateOrderNo());
        OrderDO orderDO = this.convertOrderDOFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);
        //商品销量增加
        itemService.increaseSales(itemId, amount);
        //4.返回前端
        return orderModel;
    }
    private OrderDO convertOrderDOFromOrderModel(OrderModel orderModel) throws BussinessException {
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel, orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)//流水交易号是唯一的，即使方法createOrder回滚，流水交易号不会回滚
    public String generateOrderNo()
    {
        StringBuilder stringBuilder = new StringBuilder();
        //订单号有16位
        //前8位为时间信息，年月日
        LocalDate now = LocalDate.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-","");
        stringBuilder.append(nowDate);
        //中间6位为自增序列
        int sequence = 0;
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("Order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue()+sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKey(sequenceDO);
        String str = String.valueOf(sequence);
        for(int i=0; i<6-str.length(); i++) //不足6位用0填充
        {
            stringBuilder.append(0);
        }
        stringBuilder.append(str);
        //后面2位为分库分表位，写死00
        stringBuilder.append("00");

        return stringBuilder.toString();
    }



}
