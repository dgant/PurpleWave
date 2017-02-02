package Types.BuildOrders

class BuildOrder {
  var _buildOrder:Iterable[Buildable] = List.empty

  def orders():Iterable[Buildable] = {
    return _buildOrder
  }
}
