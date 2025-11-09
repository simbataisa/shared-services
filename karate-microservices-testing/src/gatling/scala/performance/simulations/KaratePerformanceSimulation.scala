package performance.simulations

import io.gatling.core.Predef._
import com.intuit.karate.gatling.PreDef._
import scala.concurrent.duration._

object PerfOptionsParser {
  case class Options(paths: Seq[String], tags: Seq[String], env: String, users: Int)

  def parse(): Options = {
    val raw = sys.props.getOrElse("karate.options", "").trim
    val tokens = if (raw.isEmpty) Seq.empty else raw.split("\\s+").toSeq

    val paths = tokens.filterNot(_.startsWith("--")) match {
      case s if s.nonEmpty => s
      case _ => Seq("classpath:services")
    }

    val tags = tokens
      .sliding(2)
      .collect { case Seq(opt, value) if opt == "--tags" => value }
      .toSeq

    val env = sys.props.getOrElse("karate.env", "qa")
    val users = sys.props.get("threads").flatMap(s => scala.util.Try(s.toInt).toOption).getOrElse(1)

    Options(paths, tags, env, users)
  }
}

class KaratePerformanceSimulation extends Simulation {
  private val opts = PerfOptionsParser.parse()

  System.setProperty("karate.env", opts.env)
  if (opts.tags.nonEmpty) {
    val existing = sys.props.getOrElse("karate.options", "").trim
    val tagArg = s"--tags ${opts.tags.mkString(",")}".trim
    val updated = (existing.split("\\s+").toSeq ++ Seq(tagArg)).filter(_.nonEmpty).mkString(" ")
    System.setProperty("karate.options", updated)
  }

  private val protocol = karateProtocol()

  private val scenarios = opts.paths.map { p =>
    scenario(s"Performance: $p").exec(karateFeature(p))
  }

  private val durationSeconds = sys.props.get("durationSeconds").flatMap(s => scala.util.Try(s.toInt).toOption).getOrElse(0)
  private val usersPerSec = sys.props.get("usersPerSec").flatMap(s => scala.util.Try(s.toInt).toOption).getOrElse(opts.users)
  private val injection = sys.props.getOrElse("injection", if (durationSeconds > 0) "constant" else "atOnce").toLowerCase

  private def injectionSteps(users: Int) = injection match {
    case "constant" if durationSeconds > 0 => constantUsersPerSec(users).during(durationSeconds.seconds)
    case "ramp" if durationSeconds > 0 => rampUsersPerSec(0).to(users).during(durationSeconds.seconds)
    case _ => atOnceUsers(users)
  }

  setUp(
    scenarios.map(_.inject(injectionSteps(usersPerSec))).toList
  ).protocols(protocol)
}
