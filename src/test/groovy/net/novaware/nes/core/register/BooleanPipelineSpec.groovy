package net.novaware.nes.core.register

import spock.lang.Specification

class BooleanPipelineSpec extends Specification {

    def "should construct an instance"() {
        given:
        def instance = new BooleanPipeline("test")

        expect:
        instance.getName() == "test"
        instance.toString() == "test: 0"
    }

    def "should set and get immediate value"() {
        given:
        def pipeline = new BooleanPipeline("immediate")

        when:
        pipeline.set(true)

        then:
        pipeline.get()

        and:
        pipeline.set(false)

        then:
        !pipeline.get()
    }

    def "should set value with cycle delay"() {
        given:
        def pipeline = new BooleanPipeline("delayed")
        pipeline.set(true)

        when:
        pipeline.setDelayed(false, 1)

        then:
        pipeline.get()

        and:
        pipeline.cycle()

        then:
        !pipeline.get()
    }
}
