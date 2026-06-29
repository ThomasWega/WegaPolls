package me.wega.wegapolls.session.answer;

import java.util.LinkedHashSet;
import java.util.Set;

public record ChoicePollAnswer(Set<Integer> optionIds) implements PollAnswer {
    public ChoicePollAnswer(Set<Integer> optionIds) {
        this.optionIds = new LinkedHashSet<>(optionIds);
    }

    public boolean isSelected(int id) {
        return optionIds.contains(id);
    }

    public boolean addAnswer(int id) {
        return optionIds.add(id);
    }

    public boolean removeAnswer(int id) {
        return optionIds.remove(id);
    }

    public void setAnswer(int id) {
        optionIds.clear();
        optionIds.add(id);
    }
}