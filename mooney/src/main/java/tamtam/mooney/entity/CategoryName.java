package tamtam.mooney.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryName {
    EVENTS_AND_GIFTS("경조/선물", "🎁"),
    EDUCATION("교육/학습", "📚"),
    TRANSPORT("교통", "🚗"),
    FINANCE("금융", "💸"),
    ENTERTAINMENT("문화/여가", "🎭"),
    PET("반려동물", "🐶"),
    BEAUTY("뷰티/미용", "💅"),
    LIFESTYLE("생활", "🏠"),
    NIGHTLIFE("술/유흥", "🍻"),
    FOOD("식비", "🍚"),
    TRAVEL("여행/숙박", "✈️"),
    ONLINE_SHOPPING("온라인 쇼핑", "🛒"),
    HEALTH("의료/건강", "💉"),
    CHILD("자녀/육아", "👶"),
    AUTOMOBILE("자동차", "🚙"),
    HOUSING("주거/통신", "🏡"),
    CAFE("카페/간식", "☕"),
    FASHION("패션/쇼핑", "👗"),
    EXTRA("기타", "📁");

    private final String description;
    private final String icon;
}