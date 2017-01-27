package Types.Quantities

class AtLeast(quantity:Integer) extends Quantity {
  def accept(value:Integer):Boolean = {
    value >= quantity
  }
}
