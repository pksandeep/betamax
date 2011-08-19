package betamax

import betamax.storage.Tape
import spock.lang.Specification
import static org.apache.http.HttpVersion.HTTP_1_1
import betamax.storage.TapeLoadException
import java.text.ParseException
import groovy.json.JsonException

class TapeLoadingSpec extends Specification {

	def "can load a valid tape with a single interaction"() {
		given:
		def json = """\
{
	"tape": {
		"name": "single_interaction_tape",
		"interactions": [
			{
				"recorded": "2011-08-19 12:45:33 +0100",
				"request": {
					"protocol": "HTTP/1.1",
					"method": "GET",
					"uri": "http://icanhascheezburger.com/"
				},
				"response": {
					"protocol": "HTTP/1.1",
					"status": 200,
					"body": "O HAI!"
				}
			}
		]
	}
}"""
		when:
		def tape = new Tape(new StringReader(json))

		then:
		tape.name == "single_interaction_tape"
		tape.interactions.size() == 1
		tape.interactions[0].recorded == new Date(111, 7, 19, 12, 45, 33)
		tape.interactions[0].request.requestLine.protocolVersion == HTTP_1_1
		tape.interactions[0].request.requestLine.method == "GET"
		tape.interactions[0].request.requestLine.uri == "http://icanhascheezburger.com/"
		tape.interactions[0].response.statusLine.protocolVersion == HTTP_1_1
		tape.interactions[0].response.statusLine.statusCode == 200
		tape.interactions[0].response.entity.content.text == "O HAI!"
	}

	def "barfs on non-json data"() {
		given:
		def json = "THIS IS NOT JSON"

		when:
		new Tape(new StringReader(json))

		then:
		def e = thrown(TapeLoadException)
		e.cause instanceof JsonException
	}

	def "barfs on an invalid record date"() {
		given:
		def json = """\
{
	"tape": {
		"name": "invalid_timestamp_tape",
		"interactions": [
			{
				"recorded": "THIS IS NOT A DATE",
				"request": {
					"protocol": "HTTP/1.1",
					"method": "GET",
					"uri": "http://icanhascheezburger.com/"
				},
				"response": {
					"protocol": "HTTP/1.1",
					"status": 200,
					"body": "O HAI!"
				}
			}
		]
	}
}"""
		when:
		new Tape(new StringReader(json))

		then:
		def e = thrown(TapeLoadException)
		e.cause instanceof ParseException
	}

}