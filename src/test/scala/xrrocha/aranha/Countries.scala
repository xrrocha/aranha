package xrrocha.aranha

import xrrocha.util.EmbeddedLanguages._

object Countries extends HtmlCleanerFormatter {
  val countryList = Seq(
    Country(
      name = "USA", code = "us", capital = "Washington",
      currency = "USD", population = 313.9, languages = Seq("English"),
      summary = formatHtml(html"""
            |Also known as the <i>Land of the Free</i>,
            |the USA has as its motto <span class="motto">In God We Trust</span>
          """),
      flagUrl = "http://upload.wikimedia.org/wikipedia/en/thumb/a/a4/Flag_of_the_United_States.svg/30px-Flag_of_the_United_States.svg.png"),
    Country(
      name = "Canada", code = "ca", capital = "Ottawa",
      currency = "CAD", population = 34.9, languages = Seq("English", "French"),
      summary = formatHtml(html"""
            |Stating its continental span, Canada's motto is
            |<span class="motto">A Mari Usque ad Mare</span>
            |(<i>From sea to sea, D'un océan à l'autre</i>)
          """),
      flagUrl = "http://upload.wikimedia.org/wikipedia/en/thumb/c/cf/Flag_of_Canada.svg/30px-Flag_of_Canada.svg.png"),
    Country(
      name = "Mexico", code = "mx", capital = "Mexico",
      currency = "MXN", population = 116.1, languages = Seq("Spanish"),
      summary = formatHtml(html"""
            |Mexico's motto is <span class="motto">Patria, Libertad, Trabajo y Cultura</span>
            |(<i>Homeland, Freedom, Work and Culture</i>)
          """),
      flagUrl = "http://upload.wikimedia.org/wikipedia/commons/thumb/f/fc/Flag_of_Mexico.svg/30px-Flag_of_Mexico.svg.png")
  )

  case class Country(name: String,
                     code: String,
                     capital: String,
                     currency: String,
                     population: Double,
                     languages: Seq[String],
                     summary: String,
                     flagUrl: String)
  {
    def toHmtl = html"""
      |<tr>
      | <td><img src="$flagUrl"></td>
      | <td>$name</td>
      | <td align="center">$code</td>
      | <td>$capital</td>
      | <td align="center">$currency</td>
      | <td align="right">${population}M</td>
      | <td>$summary</td>
      | <td valign="top">
      |   <ul>
      |     ${languages map (l => s"<li>$l</li>") mkString "\n"}
      |   </ul>
      | </td>
      |</tr>
    """
  }

  def toHtml(countries: Seq[Country]) = html"""
      |<!DOCTYPE html>
      |<html>
      |<head lang="en">
      |    <meta charset="UTF-8">
      |    <title>Countries</title>
      |    <style>
      |       .motto {
      |         color: navy;
      |         font-weight: bold;
      |       }
      |    </style>
      |</head>
      |<body>
      |    <h1>Countries</h1>
      |    <table border="0">
      |        <thead>
      |            <tr>
      |                <th>Flag<hr></th>
      |                <th>Name<hr></th>
      |                <th>Code<hr></th>
      |                <th>Capital<hr></th>
      |                <th>Currency<hr></th>
      |                <th>Population<hr></th>
      |                <th>Summary<hr></th>
      |                <th>Languages<hr></th>
      |            </tr>
      |        </thead>
      |        <tbody>
      |           ${countries map (_.toHmtl) mkString "\n" }
      |        </tbody>
      |    </table>
      |</body>
      |</html>
    """

  import java.io.{FileOutputStream, File}

  def writeFile(filename: String): String = {
    val file = new File(filename)
    writeFile(file, toHtml(countryList))
    file.toURI.toString
  }

  def writeFile(file: File, content: String) {
    val out = new FileOutputStream(file)
    out.write(content.getBytes)
    out.flush()
    out.close()
  }
}
