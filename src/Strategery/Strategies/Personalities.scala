package Strategery.Strategies

import Lifecycle.With

object Personalities {
  object PersonalityBase extends Personality {
    override def matches: Boolean = ! PersonalityCheese.matches
  }
  object PersonalityCheese extends Personality {
    override def matches: Boolean = With.self.name.toLowerCase.contains("cheese")
  }
  object PersonalityHuman extends Personality {
    override def matches: Boolean = With.configuration.humanMode
  }
}
