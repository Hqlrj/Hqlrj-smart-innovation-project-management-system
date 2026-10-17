package org.example.springbootproject.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.springbootproject.pojo.AiVectorChunk;

import java.util.List;

@Mapper
public interface AiVectorChunkMapper {

    void insert(AiVectorChunk chunk);

    void deleteBySource(@Param("sourceType") String sourceType, @Param("sourceId") Integer sourceId);

    List<AiVectorChunk> listForSearch(@Param("college") String college,
                                      @Param("projectId") Integer projectId);
}
