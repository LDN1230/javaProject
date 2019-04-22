package ldn.dao;

import ldn.DataObject.SequenceDO;

public interface SequenceDOMapper {
    int deleteByPrimaryKey(String name);

    int insert(SequenceDO record);

    int insertSelective(SequenceDO record);

    SequenceDO selectByPrimaryKey(String name);
    SequenceDO getSequenceByName(String name);
    int updateByPrimaryKeySelective(SequenceDO record);

    int updateByPrimaryKey(SequenceDO record);
}