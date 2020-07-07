package co.astrnt.qasdk.dao;

import co.astrnt.qasdk.type.CustomField;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by deni rohimat on 09/04/18.
 * Edited by deni rohimat on 06/07/20.
 */
public class CustomFieldApiDao extends RealmObject {
    @PrimaryKey
    private long id;
    private String label;
    private int is_mandatory;
    private String description;
    private int input_type;
    private String input_name;
    private int minWords;
    private int maxWords;
    private RealmList<String> options;
    private int hasMultiple;
    private int maxOptions;

    //Supported
    private String answer;
    private RealmList<String> answers;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isMandatory() {
        return is_mandatory != 0;
    }

    public void setIs_mandatory(int is_mandatory) {
        this.is_mandatory = is_mandatory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getInput_type() {
        return input_type;
    }

    public void setInput_type(int input_type) {
        this.input_type = input_type;
    }

    public @CustomField
    String getInput_name() {
        return input_name;
    }

    public void setInput_name(@CustomField String input_name) {
        this.input_name = input_name;
    }

    public int getMinWords() {
        return minWords;
    }

    public void setMinWords(int minWords) {
        this.minWords = minWords;
    }

    public int getMaxWords() {
        return maxWords;
    }

    public void setMaxWords(int maxWords) {
        this.maxWords = maxWords;
    }

    public RealmList<String> getOptions() {
        return options;
    }

    public void setOptions(RealmList<String> options) {
        this.options = options;
    }

    public boolean getHasMultiple() {
        return hasMultiple != 0;
    }

    public void setHasMultiple(int hasMultiple) {
        this.hasMultiple = hasMultiple;
    }

    public int getMaxOptions() {
        return maxOptions;
    }

    public void setMaxOptions(int maxOptions) {
        this.maxOptions = maxOptions;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public RealmList<String> getAnswers() {
        return answers;
    }

    public void setAnswers(RealmList<String> answers) {
        this.answers = answers;
    }
}
