xquery version "1.0-ml";

module namespace ask = "http://marklogic.com/rest-api/transform/ask";


declare default function namespace "http://www.w3.org/2005/xpath-functions";
declare namespace html = "http://www.w3.org/1999/xhtml";
declare namespace search = "http://marklogic.com/appservices/search";

(: this input transform for questions
 : adds the document IRI as ID.
 : sets a creation timestamp,
 : creates empty comments and answers,
 : (and stores native JSON, working on facade JSON, which will break soon.
 :  refactor to resource extension)
 :)
declare function ask:transform(
  $context as map:map,
  $params as map:map,
  $content as document-node())
as document-node() {
    let $root := $content/node()
    let $uri := map:get($params, "uri")
    let $username := map:get($params, "userName")

    let $_ := xdmp:log(("PARAMS", $params))

    (: get the user that matches parameter :)
    let $user := cts:search(collection(), cts:json-property-value-query("userName", $username))
    let $json-doc := map:new( (
                for $n in $root/* 
                return 
                map:entry(local-name($n), data($n)),
                map:entry("creationDate", current-dateTime()),
                map:entry("comments", json:array()),
                map:entry("answers", json:array()),
                map:entry("owner", 
                    map:new((
                        map:entry("userName", $user/userName),
                        map:entry("id", $user/id),
                        map:entry("displayName", $user/displayName)
                    )))
                ) )
    return
        document {
            xdmp:to-json( $json-doc )
        }
};
