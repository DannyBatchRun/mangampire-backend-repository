package org.altervista.mangampire.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;

@Data
@ToString
public class Register {

    private long idRegister;
    private String publisher;
    private BigDecimal cash;

}
