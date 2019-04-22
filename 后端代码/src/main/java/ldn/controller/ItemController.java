package ldn.controller;

import ldn.controller.ViewObject.ItemVO;
import ldn.error.BussinessException;
import ldn.error.EmBussinessError;
import ldn.response.CommonReturnType;
import ldn.service.ItemService;
import ldn.service.model.ItemModel;
import ldn.controller.ViewObject.ItemVO;
import ldn.error.BussinessException;
import ldn.response.CommonReturnType;
import ldn.service.ItemService;
import ldn.service.model.ItemModel;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller("item")
@RequestMapping("/item")
//@CrossOrigin
public class ItemController extends BaseController{
    @Autowired
    private ItemService itemService;
    //创建商品的controller
    @RequestMapping(value = "/createItem",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    @CrossOrigin(allowCredentials = "true", allowedHeaders = "*'")
    public CommonReturnType createItem(@RequestParam(name = "title" )String title,
                                       @RequestParam(name = "description" )String description,
                                       @RequestParam(name = "price" ) BigDecimal price,
                                       @RequestParam(name = "stock" )Integer stock,
                                       @RequestParam(name = "imgUrl" )String imgUrl) throws BussinessException {
        //封装service请求用来创建商品
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setPrice(price);
        itemModel.setDescription(description);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);

        ItemModel itemModelForReturn = itemService.createItem(itemModel);
        ItemVO itemVO = convertVOFromModel(itemModelForReturn);
        return CommonReturnType.create(itemVO);
    }
    private ItemVO convertVOFromModel(ItemModel itemModel)
    {
        if(itemModel == null)
            return null;
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel, itemVO);
        if(itemModel.getPromoModel() != null)//商品有秒杀活动
        {
            itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
            itemVO.setPromoId(itemModel.getPromoModel().getId());
            itemVO.setStartDate(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
            itemVO.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else {//没有秒杀活动
            itemVO.setPromoStatus(0);
        }
        return itemVO;
    }

    //商品列表浏览
    @RequestMapping(value = "/list",method = {RequestMethod.GET})
    @ResponseBody
    @CrossOrigin(allowCredentials = "true", allowedHeaders = "*'")
    public CommonReturnType listItem()
    {
        List<ItemModel> itemModelList = itemService.listItem();
        List<ItemVO> itemVOList = itemModelList.stream().map(itemModel -> {
            ItemVO itemVO = this.convertVOFromModel(itemModel);
            return itemVO;
        }).collect(Collectors.toList());
        return CommonReturnType.create(itemVOList);
    }


    //商品详情浏览
    @RequestMapping(value = "/getItem",method = {RequestMethod.GET})
    @ResponseBody
    @CrossOrigin(allowCredentials = "true", allowedHeaders = "*'")
    public CommonReturnType getItem(@RequestParam(name = "id") Integer id) throws BussinessException {
        ItemModel itemModel = itemService.getItemById(id);
        if(itemModel == null)
            throw new BussinessException(EmBussinessError.PARANETDER_VALIDATION_ERROR,"商品不存在");
        ItemVO itemVO = this.convertVOFromModel(itemModel);
        return CommonReturnType.create(itemVO);
    }

}
