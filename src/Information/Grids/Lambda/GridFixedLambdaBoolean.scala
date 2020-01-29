package Information.Grids.Lambda

class GridFixedLambdaBoolean(lambda: (Int) => Boolean) extends AbstractGridFixedLambda[Boolean](false, lambda) {
  override protected var values: Array[Boolean] = indices.map(lambda).toArray
}
