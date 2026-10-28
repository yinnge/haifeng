package com.haifeng.app.vo.favorite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteCountItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String targetType;

    private String label;

    private Long count;
}
