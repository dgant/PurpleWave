package Types.Quantities

class Exactly(quantity:Integer) extends Quantity {
  def accept(value:Integer):Boolean = {
    value == quantity
  }
}
