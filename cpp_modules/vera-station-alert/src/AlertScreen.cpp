#include "AlertScreen.hpp"
#include "FontHelpers.hpp"

#include <format>

const float ALERT_CODE_FONT_SIZE = 48.0f;
const float ALERT_ADDRESS_FONT_SIZE = ALERT_CODE_FONT_SIZE * 1.25;
const float ALERT_DETAILS_FONT_SIZE = ALERT_ADDRESS_FONT_SIZE;
const float ALERT_UNITS_FONT_SIZE = ALERT_ADDRESS_FONT_SIZE;
const float TIMER_FONT_SIZE = ALERT_CODE_FONT_SIZE;

const RGBAColor BACKGROUND_COLOR = RGBA(36, 37, 39);
const RGBAColor ALERT_CODE_TEXT_COLOR = RGBA(255, 255, 255);
const RGBAColor ALERT_DESCRIPTION_TEXT_COLOR = RGBA(255, 255, 255);
const RGBAColor ALERT_PRIO_A_COLOR = RGBA(231, 24, 11);
const RGBAColor ALERT_PRIO_B_COLOR = RGBA(225, 113, 43);
const RGBAColor ALERT_PRIO_C_COLOR = RGBA(255, 210, 48);
const RGBAColor ALERT_PRIO_D_COLOR = RGBA(42, 166, 62);
const RGBAColor ALERT_PRIO_N_COLOR = RGBA(21, 93, 252);
const RGBAColor ALERT_ADDRESS_COLOR = RGBA(255, 255, 255);
const RGBAColor ALERT_DETAILS_COLOR = RGBA(255, 255, 255);
const RGBAColor ALERT_UNITS_COLOR = RGBA(255, 255, 255);
const RGBAColor TIMER_COLOR = RGBA(255, 255, 255);

static RGBAColor GetUrgencyColor(const std::string &assignmentUrgency)
{
    if (assignmentUrgency == "A")
    {
        return ALERT_PRIO_A_COLOR;
    }
    else if (assignmentUrgency == "B")
    {
        return ALERT_PRIO_B_COLOR;
    }
    else if (assignmentUrgency == "C")
    {
        return ALERT_PRIO_C_COLOR;
    }
    else if (assignmentUrgency == "D")
    {
        return ALERT_PRIO_D_COLOR;
    }
    else
    {
        return ALERT_PRIO_N_COLOR;
    }
}

std::string Join(const std::vector<std::string> &v, const std::string &delimiter)
{
    std::string out;
    for (const auto &s : v)
    {
        if (!out.empty())
            out += delimiter;
        out += s;
    }
    return out;
}

AlertScreen::AlertScreen(const RendererPtr &renderer) : renderer(renderer)
{
    textEngine = TextEnginePtr{TTF_CreateRendererTextEngine(renderer.get()), SDLDeleter{}};
    SDLCheck(textEngine != nullptr, "TTF_CreateRendererTextEngine");
    codeAndPrioFont = CreateRobotoBlack(ALERT_CODE_FONT_SIZE);
    assignmentDescriptionFont = CreateRobotoSemiBold(ALERT_CODE_FONT_SIZE);
    addressFont = CreateRobotoRegular(ALERT_ADDRESS_FONT_SIZE);
    detailsFont = CreateRobotoMedium(ALERT_DETAILS_FONT_SIZE);
    unitsFont = CreateRobotoMedium(ALERT_UNITS_FONT_SIZE);
    timerFont = CreateRobotoMedium(TIMER_FONT_SIZE);
    size = GetRendererSize(renderer);
}

void AlertScreen::Paint(const Alert &alert)
{
    ClearBackground(renderer, BACKGROUND_COLOR);

    // TODO Wrapping or auto-scrolling for long texts!
    // TODO Adjust font weights and sizes
    // TODO Colored boxes around various sections to make them easier to find
    // TODO A layout component with boxes with various heights?
    // TODO Timer

    // Alert code
    auto codeAndPrio = CreateText(textEngine, codeAndPrioFont, alert.assignment_code + alert.assignment_urgency, ALERT_CODE_TEXT_COLOR);
    auto codeAndPrioSize = GetTextSize(codeAndPrio);
    PaintFilledRect(renderer, 10, 10, codeAndPrioSize.width + 20, codeAndPrioSize.height, GetUrgencyColor(alert.assignment_urgency));
    PaintText(codeAndPrio, 20, 10);

    // Timer
    auto now = Timestamp::clock::now();
    auto timeSinceAlert = now - alert.timestamp;
    std::chrono::hh_mm_ss hms{timeSinceAlert};
    auto timer = CreateText(textEngine, timerFont, std::format("{:02}:{:02}:{:02}", hms.hours().count(), hms.minutes().count(), hms.seconds().count()), TIMER_COLOR);
    auto timerSize = GetTextSize(timer);
    PaintText(timer, size.width - timerSize.width - 10, 10);

    // Alert text
    auto assignmentDescription = CreateText(textEngine, assignmentDescriptionFont, alert.assignment_description, ALERT_DESCRIPTION_TEXT_COLOR);
    PaintText(assignmentDescription, codeAndPrioSize.width + 40, 10);

    // Address
    auto address = CreateText(textEngine, addressFont, alert.municipality + ", " + alert.address, ALERT_ADDRESS_COLOR);
    auto addressSize = GetTextSize(address);
    PaintText(address, 10, codeAndPrioSize.height + 30);

    // Details
    auto details = CreateText(textEngine, detailsFont, alert.details, ALERT_DETAILS_COLOR);
    auto detailsSize = GetTextSize(details);
    PaintText(details, 10, codeAndPrioSize.height + 60 + addressSize.height);

    // Units
    auto units = CreateText(textEngine, unitsFont, Join(alert.units, ", "), ALERT_UNITS_COLOR);
    PaintText(units, 10, codeAndPrioSize.height + 80 + addressSize.height + detailsSize.height);

    // Paint everything
    SDL_RenderPresent(renderer.get());
}
