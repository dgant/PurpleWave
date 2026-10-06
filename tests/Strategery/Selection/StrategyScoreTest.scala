package Strategery.Selection

/** Standalone regression tests; run with scripts/test_strategy_score.ps1. */
object StrategyScoreTest extends App {
  import StrategyScore.{Game, Model}

  private var checks = 0
  private def check(name: String)(condition: => Boolean): Unit = {
    assert(condition, name)
    checks += 1
    println("PASS " + name)
  }
  private def close(a: Double, b: Double, tolerance: Double = 1e-6): Boolean = math.abs(a - b) < tolerance
  private val dt = Seq("GateCore", "DT")
  private val robo = Seq("GateCore", "Robo")
  private def fit(games: Seq[Game], prior: Double = 0.65, halfLife: Double = 30.0, recent: Int = 3): Model =
    StrategyScore.fit(games, prior, halfLife, recent)

  check("empty history returns the opponent target") {
    close(fit(Seq.empty)(dt), 0.65)
  }
  val oneWin = fit(Seq(Game(true, dt)))
  check("one win is regularized, not scored as a 100% exploit") {
    oneWin(dt) > 0.65 && oneWin(dt) < 0.85
  }
  check("an established winner beats an untried continuation without forced exploration") {
    val winner = fit(Vector.fill(30)(Game(true, dt)))
    winner(dt) > winner(robo) && winner(dt) > 0.9
  }
  val mediocre = Vector.tabulate(60)(i => Game(i % 20 < 11, robo))
  check("a below-target continuation ranks below an untried alternative sharing its opener") {
    val model = fit(mediocre)
    model(dt) > model(robo)
  }
  check("raising the opponent target raises the score of untried alternatives") {
    fit(mediocre, prior = 0.8)(dt) > fit(mediocre, prior = 0.55)(dt)
  }
  val mixed = Vector.tabulate(120) { i =>
    if (i % 2 == 0) Game(i % 10 != 0, dt) else Game(i % 4 == 1, robo)
  }
  val mixedModel = fit(mixed)
  check("shared opener does not dilute the ranking between strong DT and mediocre Robo") {
    mixedModel(dt) > mixedModel(robo) + 0.15
  }
  check("a recent defeat has more influence than an ordinary equally recent observation") {
    val games = Game(false, dt) +: Vector.fill(10)(Game(true, dt))
    fit(games)(dt) < fit(games, recent = 0)(dt)
  }
  check("a swapped-in strategy receives the outcome; absent original strategy does not") {
    val model = fit(Seq(Game(false, robo)))
    model.effects("Robo") < 0.0 && !model.effects.contains("DT") && model(robo) < model(dt)
  }
  check("duplicate active tags never duplicate a game's outcome") {
    val duplicate = fit(mixed.map(game => game.copy(strategies = game.strategies ++ game.strategies)))
    close(duplicate(dt), mixedModel(dt)) && close(duplicate(robo), mixedModel(robo))
  }
  check("strategy ordering does not change the fitted score") {
    close(fit(mixed.map(game => game.copy(strategies = game.strategies.reverse)))(dt.reverse), mixedModel(dt))
  }
  check("old evidence vanishes instead of retaining a permanent weight floor") {
    val aged = Vector.fill(600)(Game(true, Seq.empty)) ++ Vector.fill(1000)(Game(false, dt))
    close(fit(aged)(dt), 0.65, 1e-4)
  }
  check("aging includes games where no recognized strategy was active") {
    val loss = Game(false, dt)
    val aged = Vector.fill(30)(Game(true, Seq.empty)) :+ loss
    fit(aged, recent = 0)(dt) > fit(Seq(loss), recent = 0)(dt)
  }
  check("short-horizon decay responds faster to a reversal") {
    val changed = Vector.fill(6)(Game(false, dt)) ++ Vector.fill(1000)(Game(true, dt))
    fit(changed, halfLife = 3.0)(dt) < fit(changed, halfLife = 30.0)(dt)
  }
  check("invalid and extreme configuration inputs still produce finite interior scores") {
    for (prior <- Seq(0.0, 1.0, Double.NaN, Double.PositiveInfinity);
         halfLife <- Seq(0.0, -1.0, Double.NaN, Double.PositiveInfinity)) {
      val score = fit(mixed, prior, halfLife)(dt)
      assert(score > 0.0 && score < 1.0 && !score.isNaN)
    }
    true
  }
  check("the fitted coefficients satisfy the penalized likelihood's first-order conditions") {
    mixedModel.effects.forall { case (name, effect) =>
      val gradient = effect + mixed.zipWithIndex.filter(_._1.strategies.contains(name)).map { case (game, age) =>
        val weight = math.pow(0.5, age / 30.0) * (if (!game.won && age < 3) 2.0 else 1.0)
        weight * (mixedModel(game.strategies) - (if (game.won) 1.0 else 0.0))
      }.sum
      math.abs(gradient) < 1e-5
    }
  }
  check("perfectly correlated tags share evidence symmetrically") {
    close(oneWin.effects("GateCore"), oneWin.effects("DT"))
  }

  println(f"Illustrative scores: one-win DT=${oneWin(dt)}%.3f; mixed DT=${mixedModel(dt)}%.3f, Robo=${mixedModel(robo)}%.3f")
  val start = System.nanoTime()
  val stress = Vector.tabulate(2000)(i => Game(i % 3 != 0, Seq("Opener" + i % 5, "Tech" + i % 17, "Followup" + i % 11)))
  val stressModel = fit(stress)
  check("a long, sparse history produces bounded scores") {
    stress.forall(game => stressModel(game.strategies) > 0.0 && stressModel(game.strategies) < 1.0)
  }
  println(f"2,000-game / 33-effect fit and predictions: ${(System.nanoTime() - start) / 1e6}%.1f ms")
  println(s"$checks strategy scoring checks passed.")
}
