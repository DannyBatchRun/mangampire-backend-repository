package org.altervista.mangampire.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;

@ToString
@Data
public class Card {

    private long id;

    private long idCardHolder;
    private String cardNumber;
    private String cardHolderName;
    private String cardHolderSurname;
    private String cardExpire;
    private Integer cvv;
    private BigDecimal balance;

    public Card() {
    }

    public Card(long idCardHolder, String cardNumber, String cardHolderName, String cardHolderSurname, String cardExpire, Integer cvv, BigDecimal balance) {
        this.idCardHolder = idCardHolder;
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.cardHolderSurname = cardHolderSurname;
        this.cardExpire = cardExpire;
        this.cvv = cvv;
        this.balance = balance;
    }
}