package com.suibian.model.dto.thumb;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
public class ThumbTempCacheDTO {

    /**
     * 1:点赞 -1:取消点赞
     */
    private Integer type;

    /**
     *  时间
     */
    private String time;
}
