package Strategery.Selection

/** The scoring model only; selection policies and historical strategy attribution are unchanged. */
private[Selection] object StrategyScore {

  case class Game(won: Boolean, strategies: Seq[String])

  case class Model(priorLogOdds: Double, effects: Map[String, Double]) {
    def apply(strategies: Seq[String]): Double =
      sigmoid(priorLogOdds + strategies.distinct.map(effects.getOrElse(_, 0.0)).sum)
  }

  private def sigmoid(logOdds: Double): Double = 1.0 / (1.0 + math.exp(-logOdds))

  private def finite(value: Double): Boolean = !value.isNaN && !value.isInfinity

  /** Games must be newest first. Age counts opponent encounters, including games without active tags. */
  def fit(games: Seq[Game], targetWinrate: Double, historyHalfLife: Double, recentLossGames: Int): Model = {
    val prior = if (finite(targetWinrate)) math.max(0.01, math.min(0.99, targetWinrate)) else 0.5
    val priorLogOdds = math.log(prior / (1.0 - prior))
    val halfLife = if (finite(historyHalfLife) && historyHalfLife > 0.0) historyHalfLife else 3.0
    val names = games.flatMap(_.strategies).distinct.sorted
    val indices = names.zipWithIndex.toMap
    val rows = games.map(_.strategies.distinct.map(indices).toArray).toArray
    val outcomes = games.map(game => if (game.won) 1.0 else 0.0).toArray
    val weights = games.zipWithIndex.map { case (game, age) =>
      // True exponential forgetting: obsolete evidence eventually vanishes, even after long pretraining.
      val recency = math.pow(0.5, age / halfLife)
      // A deliberate risk-sensitive tilt, not a calibrated probability: recent defeats may mean a counter.
      recency * (if (!game.won && age < recentLossGames) 2.0 else 1.0)
    }.toArray

    // One Bernoulli likelihood per game, with additive effects for its realized, active strategy tags.
    // Joint fitting separates a continuation's contribution from its shared opener without treating
    // overlapping components as independent observations. Missing/inactive tags receive no attribution.
    // Minimize sum_i w_i * logLoss(y_i, sigmoid(logit(target) + x_i * beta)) + sum_j beta_j^2 / 2.
    // Independent N(0, 1) effect priors regularize sparse and perfectly correlated strategies; an
    // unobserved effect stays zero. This is a point estimate, with no uncertainty/exploration bonus.
    val precision = 1.0
    val effects = Array.fill(names.size)(0.0)
    val predictions = Array.fill(games.size)(priorLogOdds)
    val members = Array.fill(names.size)(scala.collection.mutable.ArrayBuffer.empty[Int])
    rows.indices.foreach(row => rows(row).foreach(column => members(column) += row))
    val curvatureBounds = members.map(group => precision + 0.25 * group.map(weights(_)).sum)

    // Coordinate descent using the global logistic curvature bound p(1-p) <= 1/4.
    // Each step decreases the convex objective without a line search or unstable Hessian inversion.
    var iteration = 0
    var largestStep = Double.PositiveInfinity
    while (iteration < 200 && largestStep > 1e-7) {
      largestStep = 0.0
      var column = 0
      while (column < effects.length) {
        var gradient = precision * effects(column)
        members(column).foreach(row =>
          gradient += weights(row) * (sigmoid(predictions(row)) - outcomes(row)))
        val step = -gradient / curvatureBounds(column)
        effects(column) += step
        members(column).foreach(row => predictions(row) += step)
        largestStep = math.max(largestStep, math.abs(step))
        column += 1
      }
      iteration += 1
    }
    Model(priorLogOdds, names.zip(effects).toMap)
  }
}
