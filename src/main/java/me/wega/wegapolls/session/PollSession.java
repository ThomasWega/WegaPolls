package me.wega.wegapolls.session;

import lombok.Data;
import me.wega.wegapolls.poll.PollDefinition;
import me.wega.wegapolls.poll.PollPage;
import me.wega.wegapolls.session.answer.PollAnswer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Data
public class PollSession {
    private final PollDefinition poll;
    private int pageIndex = 0;
    private final List<PollAnswer> answers = new ArrayList<>();

    public PollPage getPollPage() {
        return poll.getPage(pageIndex);
    }

    public @Nullable PollAnswer getPageAnswer() {
        return pageIndex < answers.size() ? answers.get(pageIndex) : null;
    }

    public void setPageAnswer(@Nullable PollAnswer answer) {
        if (answer == null) {
            answers.remove(pageIndex);
            return;
        }
        if (pageIndex >= answers.size())
            answers.add(answer);
        else answers.set(pageIndex, answer);
    }
}