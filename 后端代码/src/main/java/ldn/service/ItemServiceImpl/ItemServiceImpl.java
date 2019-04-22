package ldn.service.ItemServiceImpl;

import ldn.DataObject.ItemDO;
import ldn.DataObject.ItemStockDO;
import ldn.dao.ItemDOMapper;
import ldn.dao.ItemStockDOMapper;
import ldn.error.BussinessException;
import ldn.error.EmBussinessError;
import ldn.service.ItemService;
import ldn.service.PromoService;
import ldn.service.model.ItemModel;
import ldn.service.model.PromoModel;
import ldn.validator.ValidationImpl;
import ldn.validator.ValidationResult;
import ldn.DataObject.ItemDO;
import ldn.DataObject.ItemStockDO;
import ldn.dao.ItemDOMapper;
import ldn.dao.ItemStockDOMapper;
import ldn.error.BussinessException;
import ldn.error.EmBussinessError;
import ldn.service.ItemService;
import ldn.service.model.ItemModel;
import ldn.validator.ValidationImpl;
import ldn.validator.ValidationResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ValidationImpl validator;
    @Autowired
    private ItemDOMapper itemDOMapper;
    @Autowired
    private ItemStockDOMapper itemStockDOMapper;
    @Autowired
    private PromoService promoService;
    private ItemDO convertItemDOFromItemModel(ItemModel itemModel)
    {
        if(itemModel == null)
            return null;
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel, itemDO);
        itemDO.setPrice(itemModel.getPrice().doubleValue());//itemDO的price是double型
        return itemDO;
    }
    private ItemStockDO convertItemStockDOFromItemModel(ItemModel itemModel)
    {
        if(itemModel == null)
            return null;
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());
        return itemStockDO;
    }
    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BussinessException {
        //校验入参
        ValidationResult result = validator.validate(itemModel);
        if(result.isHasErrors())
        {
            throw new  BussinessException(EmBussinessError.PARANETDER_VALIDATION_ERROR, result.getErrMsg());
        }
        //转化itemModel->DataObject
        ItemDO itemDO = this.convertItemDOFromItemModel(itemModel);
        //写入数据库
        itemDOMapper.insertSelective(itemDO);
        itemModel.setId(itemDO.getId());
        ItemStockDO itemStockDO = this.convertItemStockDOFromItemModel(itemModel);
        itemStockDOMapper.insertSelective(itemStockDO);
        itemStockDOMapper.setItemId();
        //返回创建完成的对象
         return this.getItemById(itemModel.getId());
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOList = itemDOMapper.listItem();
        List<ItemModel> itemModelList = itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.convertModelFromDataObject(itemDO, itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if(itemDO == null)
            return null;
        //获取库存数量
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
        ItemModel itemModel  = convertModelFromDataObject(itemDO,itemStockDO);
        //获取活动商品信息
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if(promoModel != null && promoModel.getStatus().intValue() != 3)
        {
            itemModel.setPromoModel(promoModel);
        }
        return itemModel;
    }
    private ItemModel convertModelFromDataObject(ItemDO itemDO, ItemStockDO itemStockDO)
    {
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO, itemModel);
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;
    }
    @Override
    public boolean decreaseStock(Integer itemId, Integer amount) throws BussinessException {
        int affectedRow = itemStockDOMapper.decreaseStock(itemId, amount);
        if(affectedRow > 0)//更新库成功
            return true;
        else//更新库失败
            return false;
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BussinessException {
        itemDOMapper.increaseSales(itemId, amount);
    }
}
