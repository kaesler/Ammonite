#!/usr/bin/env amm
import ammonite.ops._
import scalaj.http._

@main
def shorten(longUrl: String) = {
  println("shorten longUrl " + longUrl)
  val shortUrl = Http("https://git.io")
    .postForm(Seq("url" -> longUrl))
    .asString
    .headers("Location")
    .head
  println("shorten shortUrl " + shortUrl)
  shortUrl
}
@main
def apply(uploadedFile: Path,
          tagName: String,
          uploadName: String,
          authKey: String): String = {
  val parsed = upickle.json.read(
    Http("https://api.github.com/repos/lihaoyi/Ammonite/releases")
      .header("Authorization", "token " + authKey)
      .asString.body
  )

  pprint.log(parsed, height=9999)

  val snapshotReleaseId =
    parsed.arr
      .find(_("tag_name").str == tagName)
      .get("id")
      .num.toInt


  val uploadUrl =
    s"https://uploads.github.com/repos/lihaoyi/Ammonite/releases/" +
      s"$snapshotReleaseId/assets?name=$uploadName"

  val res = Http(uploadUrl)
    .header("Content-Type", "application/octet-stream")
    .header("Authorization", "token " + authKey)
    .timeout(connTimeoutMs = 5000, readTimeoutMs = 60000)
    .postData(read.bytes! uploadedFile)
    .asString

  pprint.log(res.body, height=9999)
  val longUrl = upickle.json.read(res.body)("browser_download_url").str

  println("Long Url " + longUrl)

  val shortUrl = shorten(longUrl)

  println("Short Url " + shortUrl)
  shortUrl
}