package com.haifeng.admin.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultVO {

    private Integer total;

    private Integer success;

    private Integer failed;

    private List<String> errors;
}
