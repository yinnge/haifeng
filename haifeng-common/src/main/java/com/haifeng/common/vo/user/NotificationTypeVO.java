package com.haifeng.common.vo.user;

import com.haifeng.common.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTypeVO {

    private String value;

    private String desc;

    public static List<NotificationTypeVO> all() {
        return Arrays.stream(NotificationType.values())
                .map(e -> new NotificationTypeVO(e.getValue(), e.getDesc()))
                .collect(Collectors.toList());
    }
}
