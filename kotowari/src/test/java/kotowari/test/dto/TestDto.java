package kotowari.test.dto;

import java.io.Serializable;

public class TestDto implements Serializable {
    private int a;
    private String b;

    public TestDto() {
    }

    public int getA() {
        return this.a;
    }

    public String getB() {
        return this.b;
    }

    public void setA(int a) {
        this.a = a;
    }

    public void setB(String b) {
        this.b = b;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof TestDto)) return false;
        final TestDto other = (TestDto) o;
        if (!other.canEqual(this)) return false;
        if (this.getA() != other.getA()) return false;
        final Object this$b = this.getB();
        final Object other$b = other.getB();
        if (this$b == null ? other$b != null : !this$b.equals(other$b)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getA();
        final Object $b = this.getB();
        result = result * PRIME + ($b == null ? 43 : $b.hashCode());
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof TestDto;
    }

    public String toString() {
        return "TestDto(a=" + this.getA() + ", b=" + this.getB() + ")";
    }
}
