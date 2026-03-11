package TestShop;

import model.Money;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestMoney {
    // on a 100 d'argent pour les tests
    Money money = new Money(100);

    @Test
    public void testGetAmount() {
        Assertions.assertEquals(100, money.getAmount());
    }

    //teste de l'ajout d'argent
    @Test
    public void testAdd() {
        Money other = new Money(50);
        money.add(other);
        Assertions.assertEquals(150, money.getAmount());
    }

    //teste de la soustraction d'argent
    @Test
    public void testSubtract() {
        Money other = new Money(30);
        money.subtract(other);
        Assertions.assertEquals(70, money.getAmount());
    }
}