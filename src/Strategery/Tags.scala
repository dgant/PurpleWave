package Strategery

import Debugging.ToString
import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Strategery.Strategies.Strategy
import Utilities.?

object Tags {

  def matchesStrategy(tag: String, strategy: Strategy): Boolean = {
    tag == strategy.toString
  }

  def strategySelected(tag: String): Option[Strategy] = {
    val cleanedTag = tag.stripPrefix("[").stripSuffix("]")
    With.strategy.strategiesAll.find(matchesStrategy(cleanedTag, _))
  }

  def strategyActive(tag: String): Option[Strategy] = {
    With.strategy.strategiesAll.find(matchesStrategy(tag, _))
  }

  def strategiesSelected(tags: Seq[String]): Seq[Strategy] = {
    tags.flatMap(strategySelected)
  }

  def strategiesActive(tags: Seq[String]): Seq[Strategy] = {
    tags.flatMap(strategyActive)
  }

  def tagStrategy(strategy: Strategy): String = {
    ?(strategy.active, f"$strategy", f"[$strategy]")
  }



  def matchesFingerprint(tag: String, fingerprint: Fingerprint): Boolean = {
    tagFingerprint(fingerprint) == cleanFingerprintTag(tag)
  }

  def fingerprint(tag: String): Option[Fingerprint] = {
    With.fingerprints.all.find(_.toString == tag)
  }

  def fingerprints(tags: Seq[String]): Seq[Fingerprint] = {
    tags.flatMap(fingerprint)
  }

  def tagFingerprint(fingerprint: Fingerprint): String = {
    cleanFingerprintTag(ToString(fingerprint))
  }

  def cleanFingerprintTag(tag: String): String = tag
    .replaceAll("Fingerprint", "Finger")
    .replaceAll("Finger", "&")
}
