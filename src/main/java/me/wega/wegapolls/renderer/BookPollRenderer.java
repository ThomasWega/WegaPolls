package me.wega.wegapolls.renderer;

import me.wega.wegapolls.poll.PollDefinition;
import me.wega.wegapolls.poll.PollPage;
import me.wega.wegapolls.poll.PollQuestionMode;
import me.wega.wegapolls.session.PollSession;
import me.wega.wegapolls.session.PollSessionRegistry;
import me.wega.wegapolls.session.answer.ChoicePollAnswer;
import me.wega.wegapolls.session.answer.PollAnswer;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static me.wega.wegapolls.Msgs.msg;
import static me.wega.wegapolls.Scheduler.runSync;

public class BookPollRenderer implements PollRenderer {

    private static final int MAX_QUESTION_INLINE_LENGTH = 55;
    private static final int MAX_ANSWER_INLINE_LENGTH = 16;

    private final PollSessionRegistry sessionRegistry;

    public BookPollRenderer(PollSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void render(Player player, PollSession session) {
        player.openBook(Book.book(
                Component.text(session.getPoll().getPollName()),
                Component.text("WegaPolls"),
                List.of(buildPage(player, session))
        ));
    }

    private Component buildPage(Player player, PollSession session) {
        PollDefinition poll = session.getPoll();
        int idx = session.getPageIndex();
        PollPage page = poll.getPage(idx);
        @Nullable PollAnswer answer = session.getPageAnswer();

        return Component.empty()
                .append(buildHeader(poll.getPollName(), idx, poll.size(), page.getMode()))
                .append(buildQuestion(page.getQuestion()))
                .append(buildAnswers(player, page, answer))
                .append(buildNavBar(player, session));
    }

    private Component buildHeader(String pollName, int pageIndex, int total, PollQuestionMode mode) {
        return Component.text()
                .append(Component.text("─── ", NamedTextColor.DARK_GRAY))
                .append(Component.text(shorten(pollName, 12), NamedTextColor.DARK_PURPLE, TextDecoration.BOLD)
                        .hoverEvent(HoverEvent.showText(Component.text(pollName, NamedTextColor.LIGHT_PURPLE)))
                )
                .append(Component.text(" ───", NamedTextColor.DARK_GRAY))
                .append(Component.newline())
                .append(Component.text("Q" + (pageIndex + 1) + "/" + total, NamedTextColor.GRAY))
                .append(Component.text("  ·  ", NamedTextColor.DARK_GRAY))
                .append(Component.text(mode.getLabel(), NamedTextColor.BLUE))
                .append(Component.newline())
                .append(Component.newline())
                .build();
    }

    private Component buildQuestion(String question) {
        Component text = question.length() > MAX_QUESTION_INLINE_LENGTH
                ? Component.text(shorten(question, MAX_QUESTION_INLINE_LENGTH), NamedTextColor.GRAY)
                .hoverEvent(HoverEvent.showText(
                        Component.text("Question:\n", NamedTextColor.GRAY)
                                .append(Component.text(question, NamedTextColor.GRAY))
                ))
                : Component.text(question, NamedTextColor.GRAY);

        return Component.empty().append(text).append(Component.newline()).append(Component.newline());
    }

    private Component buildAnswers(Player player, PollPage page, @Nullable PollAnswer currentAnswer) {
        ChoicePollAnswer choice = (currentAnswer instanceof ChoicePollAnswer ca) ? ca : null;
        Component section = Component.empty();
        for (int i = 0; i < page.getOptions().size(); i++) {
            boolean selected = choice != null && choice.isSelected(i);
            section = section.append(buildAnswerLine(player, page.getOptions().get(i), i, page.getMode(), selected));
        }
        return section.append(Component.newline());
    }

    private Component buildAnswerLine(Player player, String option, int idx, PollQuestionMode mode, boolean selected) {
        boolean single = mode == PollQuestionMode.SINGLE_CHOICE;
        String bullet = selected ? (single ? "◉" : "☑") : (single ? "◯" : "☐");
        NamedTextColor bulletColor = selected ? NamedTextColor.DARK_GREEN : NamedTextColor.DARK_GRAY;
        NamedTextColor textColor = selected ? NamedTextColor.DARK_GREEN : NamedTextColor.GRAY;

        Component label = option.length() > MAX_ANSWER_INLINE_LENGTH
                ? Component.text(shorten(option, MAX_ANSWER_INLINE_LENGTH), textColor)
                .hoverEvent(HoverEvent.showText(
                        Component.text("Full answer:\n", NamedTextColor.GRAY)
                                .append(Component.text(option, textColor))
                ))
                : Component.text(option, textColor);

        return Component.text()
                .append(Component.text(bullet + " ", bulletColor))
                .append(label)
                .clickEvent(ClickEvent.callback(_ -> handleToggle(player, idx)))
                .hoverEvent(HoverEvent.showText(Component.text(
                        selected ? "Click to deselect" : "Click to select",
                        selected ? NamedTextColor.RED : NamedTextColor.GREEN)
                ))
                .build()
                .append(Component.newline());
    }

    private Component buildNavBar(Player player, PollSession session) {
        int pageIndex = session.getPageIndex();
        int total = session.getPoll().size();
        boolean isFirst = pageIndex == 0;
        boolean isLast = pageIndex == total - 1;
        boolean answered = isCurrentPageAnswered(session);

        Component back = isFirst
                ? Component.empty()
                : Component.text("[← Back]", NamedTextColor.GOLD)
                .clickEvent(ClickEvent.callback(_ -> handleNav(player, pageIndex - 1)))
                .hoverEvent(HoverEvent.showText(Component.text("Go to question " + pageIndex, NamedTextColor.GRAY)));

        Component forward = isLast
                ? (answered
                ? Component.text("[✔ Submit]", NamedTextColor.GREEN, TextDecoration.BOLD)
                .clickEvent(ClickEvent.callback(_ -> handleSubmit(player)))
                .hoverEvent(HoverEvent.showText(Component.text("Submit your answers", NamedTextColor.GREEN)))
                : Component.text("[✔ Submit]", NamedTextColor.DARK_GRAY)
                .hoverEvent(HoverEvent.showText(Component.text("Answer this question first", NamedTextColor.RED))))
                : (answered
                ? Component.text("[Next →]", NamedTextColor.GOLD)
                .clickEvent(ClickEvent.callback(_ -> handleNav(player, pageIndex + 1)))
                .hoverEvent(HoverEvent.showText(Component.text("Go to question " + (pageIndex + 2), NamedTextColor.GRAY)))
                : Component.text("[Next →]", NamedTextColor.DARK_GRAY)
                .hoverEvent(HoverEvent.showText(Component.text("Answer this question first", NamedTextColor.RED))));

        // Only add the spacer between back and forward when both are visible
        Component spacer = isFirst ? Component.empty() : Component.text("   ");

        return Component.empty()
                .append(back)
                .append(spacer)
                .append(forward)
                .append(Component.newline());
    }

    private boolean isCurrentPageAnswered(PollSession session) {
        PollAnswer answer = session.getPageAnswer();
        if (answer == null) return false;
        if (answer instanceof ChoicePollAnswer(Set<Integer> optionIds)) return !optionIds.isEmpty();
        return true;
    }

    public void handleToggle(Player player, int optionIndex) {
        PollSession session = sessionRegistry.getSession(player);
        if (session == null) return;

        PollPage page = session.getPollPage();
        if (optionIndex < 0 || optionIndex >= page.getOptions().size()) return;

        ChoicePollAnswer answer = session.getPageAnswer() instanceof ChoicePollAnswer ca
                ? ca : new ChoicePollAnswer(new LinkedHashSet<>());
        session.setPageAnswer(answer);

        if (page.getMode() == PollQuestionMode.SINGLE_CHOICE) {
            if (answer.isSelected(optionIndex))
                answer.removeAnswer(optionIndex);
            else answer.setAnswer(optionIndex);
        } else {
            if (answer.isSelected(optionIndex))
                answer.removeAnswer(optionIndex);
            else answer.addAnswer(optionIndex);
        }

        runSync(() -> render(player, session));
    }

    public void handleNav(Player player, int targetPage) {
        PollSession session = sessionRegistry.getSession(player);
        if (session == null) return;
        if (targetPage < 0 || targetPage >= session.getPoll().size()) return;

        // Block forward navigation if current question is unanswered
        if (targetPage > session.getPageIndex()) {
            if (!isCurrentPageAnswered(session)) {
                runSync(() -> player.sendMessage(msg(
                        "<red>Please answer this question before continuing.</red>"
                )));
                return;
            }
        }

        session.setPageIndex(targetPage);
        runSync(() -> render(player, session));
    }

    public void handleSubmit(Player player) {
        PollSession session = sessionRegistry.getSession(player);
        if (session == null) return;
        PollDefinition poll = session.getPoll();

        for (int i = 0; i < poll.size(); i++) {
            if (session.getAnswers().size() <= i || session.getAnswers().get(i) == null) {
                session.setPageIndex(i);
                runSync(() -> {
                    player.sendMessage(msg("<red>Please answer all questions before submitting. "
                            + "Question <yellow>" + (session.getPageIndex() + 1) + "</yellow> is unanswered.</red>"
                    ));
                    this.render(player, session);
                });
                return;
            }
        }

        sessionRegistry.markCompleted(player, poll);
        runSync(() -> {
            player.sendMessage(msg("<green>✔ Thank you! Your response to <white>"
                    + poll.getPollName() + "</white> has been recorded.</green>"
            ));
            player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
        });
    }

    private static String shorten(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max - 1) + "…";
    }
}