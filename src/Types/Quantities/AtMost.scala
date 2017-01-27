package Types.Quantities

class AtMost(quantity:Integer) extends Quantity {
  def accept(value:Integer):Boolean = {
    value <= quantity
  }
}
