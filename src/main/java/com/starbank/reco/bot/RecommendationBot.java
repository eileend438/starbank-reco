package com.starbank.reco.bot;

import com.starbank.reco.repo.UsersLookupRepository;
import com.starbank.reco.service.RecommendationService;
import com.starbank.reco.dto.ProductRecommendationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
public class RecommendationBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}") private String username;
    @Value("${telegram.bot.token}") private String token;

    private final UsersLookupRepository usersRepo;
    private final RecommendationService recommendationService;

    public RecommendationBot(UsersLookupRepository usersRepo, RecommendationService recommendationService) {
        this.usersRepo = usersRepo;
        this.recommendationService = recommendationService;
    }

    @Override public String getBotUsername() { return username; }
    @Override public String getBotToken() { return token; }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText().trim();

        if (text.equals("/start")) {
            send(chatId, """
                    Привет! Я бот рекомендаций «Стар».
                    Команда:
                    /recommend Имя Фамилия
                    """);
            return;
        }

        if (text.startsWith("/recommend")) {
            String arg = text.replaceFirst("^/recommend\\s*", "").trim();
            if (arg.isBlank()) {
                send(chatId, "Формат: /recommend Имя Фамилия");
                return;
            }
            var users = usersRepo.findByExactName(arg);
            if (users.size() != 1) {
                send(chatId, "Пользователь не найден");
                return;
            }
            var user = users.get(0);

            List<ProductRecommendationDto> recos = recommendationService.recommend(user.id());

            if (recos.isEmpty()) {
                send(chatId, "Здравствуйте " + user.name() + "\nНовых продуктов для вас пока нет.");
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Здравствуйте ").append(user.name()).append("\n\nНовые продукты для вас:\n");
            for (var r : recos) {
                sb.append("• ").append(r.name()).append("\n")
                        .append("  ").append(r.text()).append("\n\n");
            }
            send(chatId, sb.toString());
            return;
        }

        send(chatId, "Команда не понята. Используй: /recommend Имя Фамилия");
    }

    private void send(Long chatId, String text) {
        try {
            execute(SendMessage.builder().chatId(chatId.toString()).text(text).build());
        } catch (Exception e) {
        }
    }
}
