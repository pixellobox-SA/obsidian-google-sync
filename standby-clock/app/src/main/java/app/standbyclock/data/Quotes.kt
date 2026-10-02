package app.standbyclock.data

import java.time.LocalDate
import java.time.temporal.IsoFields

data class Quote(val text: String, val author: String)

/**
 * A year's worth of short, public-domain quotes built into the app. No internet needed:
 * the quote changes every Monday, based only on the date.
 */
object Quotes {
    private val all = listOf(
        Quote("You have power over your mind, not outside events. Realise this, and you will find strength.", "Marcus Aurelius"),
        Quote("The happiness of your life depends upon the quality of your thoughts.", "Marcus Aurelius"),
        Quote("Waste no more time arguing what a good man should be. Be one.", "Marcus Aurelius"),
        Quote("Very little is needed to make a happy life.", "Marcus Aurelius"),
        Quote("The best revenge is not to be like your enemy.", "Marcus Aurelius"),
        Quote("Luck is what happens when preparation meets opportunity.", "Seneca"),
        Quote("We suffer more often in imagination than in reality.", "Seneca"),
        Quote("While we are postponing, life speeds by.", "Seneca"),
        Quote("It is not that we have a short time to live, but that we waste a lot of it.", "Seneca"),
        Quote("Difficulties strengthen the mind, as labour does the body.", "Seneca"),
        Quote("No man is free who is not master of himself.", "Epictetus"),
        Quote("First say to yourself what you would be; then do what you have to do.", "Epictetus"),
        Quote("It's not what happens to you, but how you react to it that matters.", "Epictetus"),
        Quote("Wealth consists not in having great possessions, but in having few wants.", "Epictetus"),
        Quote("It does not matter how slowly you go as long as you do not stop.", "Confucius"),
        Quote("Our greatest glory is not in never falling, but in rising every time we fall.", "Confucius"),
        Quote("The man who moves a mountain begins by carrying away small stones.", "Confucius"),
        Quote("A journey of a thousand miles begins with a single step.", "Lao Tzu"),
        Quote("Nature does not hurry, yet everything is accomplished.", "Lao Tzu"),
        Quote("Knowing others is wisdom; knowing yourself is enlightenment.", "Lao Tzu"),
        Quote("We are what we repeatedly do.", "Aristotle"),
        Quote("Knowing yourself is the beginning of all wisdom.", "Aristotle"),
        Quote("Patience is bitter, but its fruit is sweet.", "Aristotle"),
        Quote("The unexamined life is not worth living.", "Socrates"),
        Quote("Be kind, for everyone you meet is fighting a hard battle.", "Ian Maclaren"),
        Quote("Well done is better than well said.", "Benjamin Franklin"),
        Quote("Lost time is never found again.", "Benjamin Franklin"),
        Quote("Energy and persistence conquer all things.", "Benjamin Franklin"),
        Quote("The secret of getting ahead is getting started.", "Mark Twain"),
        Quote("Kindness is the language which the deaf can hear and the blind can see.", "Mark Twain"),
        Quote("Go confidently in the direction of your dreams.", "Henry David Thoreau"),
        Quote("Our life is frittered away by detail. Simplify, simplify.", "Henry David Thoreau"),
        Quote("What lies behind us and what lies before us are tiny matters compared to what lies within us.", "Ralph Waldo Emerson"),
        Quote("Write it on your heart that every day is the best day in the year.", "Ralph Waldo Emerson"),
        Quote("To be yourself in a world that is constantly trying to make you something else is the greatest accomplishment.", "Ralph Waldo Emerson"),
        Quote("Whatever you are, be a good one.", "Abraham Lincoln"),
        Quote("The best way to predict your future is to create it.", "Abraham Lincoln"),
        Quote("Nothing is so strong as gentleness, nothing so gentle as real strength.", "Francis de Sales"),
        Quote("Knowing is not enough; we must apply. Willing is not enough; we must do.", "Johann Wolfgang von Goethe"),
        Quote("Whatever you can do, or dream you can, begin it.", "Johann Wolfgang von Goethe"),
        Quote("What you seek is seeking you.", "Rumi"),
        Quote("Let the beauty of what you love be what you do.", "Rumi"),
        Quote("This too shall pass.", "Persian proverb"),
        Quote("Fall seven times, stand up eight.", "Japanese proverb"),
        Quote("The best time to plant a tree was twenty years ago. The second best time is now.", "Chinese proverb"),
        Quote("If you want to go fast, go alone. If you want to go far, go together.", "African proverb"),
        Quote("Simplicity is the ultimate sophistication.", "Leonardo da Vinci"),
        Quote("I have been impressed with the urgency of doing.", "Leonardo da Vinci"),
        Quote("Life is really simple, but we insist on making it complicated.", "Confucius"),
        Quote("Do what you can, with what you have, where you are.", "Theodore Roosevelt"),
        Quote("Believe you can and you're halfway there.", "Theodore Roosevelt"),
        Quote("Dwell on the beauty of life. Watch the stars, and see yourself running with them.", "Marcus Aurelius"),
        Quote("He who has a why to live can bear almost any how.", "Friedrich Nietzsche"),
    )

    /** Same quote all week; a new one each Monday. */
    fun ofTheWeek(date: LocalDate = LocalDate.now()): Quote {
        val week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        val year = date.get(IsoFields.WEEK_BASED_YEAR)
        return all[(year * 53 + week).mod(all.size)]
    }
}
