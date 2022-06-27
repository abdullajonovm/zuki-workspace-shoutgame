package com.meinc.gameplay.domain;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;

public class DiagnosticsQuestion 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Question _question;
    private List<DiagnosticsAnswer> _answers;
    
    @JsonSerialize(using=QuestionJacksonJsonSerializer.class)
    public Question getQuestion()
    {
        return _question;
    }

    public void setQuestion(Question question)
    {
        _question = question;
    }

    public List<DiagnosticsAnswer> getAnswers()
    {
        return _answers;
    }

    public void setAnswers(List<DiagnosticsAnswer> answers)
    {
        _answers = answers;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("#").append(getQuestion().getQuestionNumber()).append(": ");
        buf.append(getQuestion().getQuestionText());
        for (DiagnosticsAnswer answer : getAnswers()) {
            buf.append("\n\t\t").append(answer);
        }

        return buf.toString();
    }

    static class QuestionJacksonJsonSerializer extends JsonSerializer<Question>
    {
        @Override
        public void serialize(Question question, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) 
        throws IOException, JsonProcessingException
        {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeFieldName("questionNumber");
            jsonGenerator.writeNumber(question.getQuestionNumber() == null ? 0 : question.getQuestionNumber());
            jsonGenerator.writeFieldName("questionText");
            jsonGenerator.writeString(question.getQuestionText());
            jsonGenerator.writeFieldName("startedDate");
            jsonGenerator.writeString(DiagnosticsData.dateToIso8601(question.getStartedDate()));
            jsonGenerator.writeFieldName("stoppedDate");
            jsonGenerator.writeString(DiagnosticsData.dateToIso8601(question.getStoppedDate()));
            jsonGenerator.writeArrayFieldStart("answers");
            for (Answer answer : question.getAnswers()) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeFieldName("answerText");
                jsonGenerator.writeString(answer.getAnswerText());
                jsonGenerator.writeFieldName("answerCorrect");
                jsonGenerator.writeBoolean(answer.isAnswerCorrect());
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
        }
    }
}
