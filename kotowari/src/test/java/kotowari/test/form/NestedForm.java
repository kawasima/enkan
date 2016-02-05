package kotowari.test.form;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * A form for testing having various types.
 *
 * @author kawasima
 */
public class NestedForm implements Serializable {
    private String strVal;
    private Integer intVal;
    private Long longVal;
    private BigDecimal decimalVal;
    private Double doubleVal;
    private Item item;
    private List<Item> itemList;
    private Item[] itemArray;

    public static class Item {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public String getStrVal() {
        return strVal;
    }

    public void setStrVal(String strVal) {
        this.strVal = strVal;
    }

    public Integer getIntVal() {
        return intVal;
    }

    public void setIntVal(Integer intVal) {
        this.intVal = intVal;
    }

    public Long getLongVal() {
        return longVal;
    }

    public void setLongVal(Long longVal) {
        this.longVal = longVal;
    }

    public BigDecimal getDecimalVal() {
        return decimalVal;
    }

    public void setDecimalVal(BigDecimal decimalVal) {
        this.decimalVal = decimalVal;
    }

    public Double getDoubleVal() {
        return doubleVal;
    }

    public void setDoubleVal(Double doubleVal) {
        this.doubleVal = doubleVal;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }

    public Item[] getItemArray() {
        return itemArray;
    }

    public void setItemArray(Item[] itemArray) {
        this.itemArray = itemArray;
    }
}
