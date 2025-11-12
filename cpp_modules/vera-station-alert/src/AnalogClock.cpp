#include <algorithm>
#include <chrono>
#include <cmath>

#include "AnalogClock.hpp"

const uint32_t CLOCK_FACE_COLOR = RGBA(24, 25, 27);
const uint32_t CLOCK_BORDER_COLOR = RGBA(54, 69, 79);
const uint32_t CLOCK_HOUR_HAND_COLOR = RGBA(255, 255, 255);
const uint32_t CLOCK_MINUTE_HAND_COLOR = RGBA(255, 255, 255);
const uint32_t CLOCK_SECOND_HAND_COLOR = RGBA(231, 24, 11);

static SDL_FPoint CalculatePointFromCenter(const SDL_FPoint &center, const float radius, const float angleRadians)
{
    auto xOffset = radius * sin(angleRadians);
    auto yOffset = radius * cos(angleRadians);
    return SDL_FPoint{(float)(center.x + xOffset),
                      (float)(center.y - yOffset)};
}

static void PaintClockFace(const RendererPtr &renderer, const SDL_FPoint &center, const float radius)
{
    // Clear
    SDL_SetRenderDrawColor(renderer.get(), 36, 37, 39, 255);
    SDL_RenderClear(renderer.get());

    // Face
    PaintFilledCircle(renderer, center.x, center.y, radius, CLOCK_FACE_COLOR);

    // TODO Ticks

    // TODO Numbers
}

static void PaintHand(const RendererPtr &renderer, const SDL_FPoint &center, const float radius, const float angleRadians, const float thickness, const uint32_t color)
{
    auto endPoint = CalculatePointFromCenter(center, radius, angleRadians);
    auto endCircleRadius = thickness / 2.0f;
    PaintLine(renderer, center.x, center.y, endPoint.x, endPoint.y, thickness, color);
    PaintFilledCircle(renderer, endPoint.x, endPoint.y, endCircleRadius, color);
}

static void PaintHands(const RendererPtr &renderer, const SDL_FPoint &center, const float radius, const std::chrono::hh_mm_ss<std::chrono::system_clock::duration> &time)
{
    auto hours = time.hours().count() % 12;
    auto minutes = time.minutes().count();
    auto seconds = time.seconds().count();

    // Hour hand
    auto hourRadians = ((hours * 60 + minutes) / 720.0f) * M_PI * 2;
    PaintHand(renderer, center, radius * 0.5, hourRadians, radius * 0.03, CLOCK_HOUR_HAND_COLOR);

    // Minute hand
    auto minuteRadians = ((minutes + seconds / 60.0f) / 60.0f) * M_PI * 2;
    PaintHand(renderer, center, radius * 0.65, minuteRadians, radius * 0.02, CLOCK_MINUTE_HAND_COLOR);

    // Second hand
    auto secondRadians = (seconds / 60.0f) * M_PI * 2;
    PaintHand(renderer, center, radius * 0.7, secondRadians, radius * 0.01, CLOCK_SECOND_HAND_COLOR);

    // Center
    PaintFilledCircle(renderer, center.x, center.y, radius * 0.04, CLOCK_SECOND_HAND_COLOR);
    PaintFilledCircle(renderer, center.x, center.y, radius * 0.02, CLOCK_MINUTE_HAND_COLOR);
}

AnalogClock::AnalogClock(const RendererPtr &renderer) : renderer(renderer)
{
    const auto size = GetRendererSize(renderer);
    center = {size.width / 2.0f, size.height / 2.0f};
    radius = std::min(size.height, size.width) / 2 - 20;

    const auto hiResHeight = size.height * 2;
    const auto hiResWidth = size.width * 2;
    hiResTexture = TexturePtr{
        SDL_CreateTexture(renderer.get(), SDL_PIXELFORMAT_RGBA8888, SDL_TEXTUREACCESS_TARGET, hiResWidth, hiResHeight),
        SDLDeleter{}
    };
    SDL_SetTextureBlendMode(hiResTexture.get(), SDL_BLENDMODE_BLEND);
}

void AnalogClock::Paint()
{
    using namespace std::chrono;
    // TODO This seems quite complex to get the current hour, minute and second. Can it be simplified?
    auto now = system_clock::now();
    auto local = zoned_time{current_zone(), now}.get_local_time();
    auto today = floor<days>(local);
    auto timeSinceMidnight = local - today;
    hh_mm_ss hms{timeSinceMidnight};
    
    // Paint in 2x (high resolution), then scale down to get anti-aliasing

    SDL_SetRenderTarget(renderer.get(), hiResTexture.get());
    SDL_SetRenderScale(renderer.get(), 2.0f, 2.0f);

    PaintClockFace(renderer, center, radius);
    PaintHands(renderer, center, radius, hms);

    SDL_SetRenderTarget(renderer.get(), nullptr);
    SDL_SetRenderScale(renderer.get(), 1.0f, 1.0f);
    RenderTexture(renderer, hiResTexture);
    SDL_RenderPresent(renderer.get());
}
