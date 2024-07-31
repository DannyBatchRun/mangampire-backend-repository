package org.altervista.mangampire.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Data
public class Register {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long idRegister;
    private String publisher;
    private BigDecimal cash;

}
