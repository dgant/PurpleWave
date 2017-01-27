package Types.Quantities

class Any(quantity:Integer) extends Quantity {
  def accept(value:Integer):Boolean = {
    value > 0;
  }
}
