package Strategery

import Strategery.Strategies.{Paper, Rock, Strategy}

class Strategist {
  
  lazy val selected: Set[Strategy] = {
    Set(
      Rock,
      Paper)
  }
}

