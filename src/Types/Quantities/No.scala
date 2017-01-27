package Types.Quantities

class No(quantity:Integer) extends Quantity {
  def accept(value:Integer):Boolean = {
    value == 0;
  }
}
