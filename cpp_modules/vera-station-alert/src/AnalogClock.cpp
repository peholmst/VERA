#include <SDL3_gfxPrimitives.h>

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

static void PaintClockFace(SDL_Renderer *renderer, const SDL_FPoint &center, const float radius)
{
    // Clear
    SDL_SetRenderDrawColor(renderer, 36, 37, 39, 255);
    SDL_RenderClear(renderer);

    // Face
    filledCircleColor(renderer, center.x, center.y, radius, CLOCK_FACE_COLOR);

    /*    // Face
        filledCircleColor(renderer, centerX, centerY, radius * 0.95, CLOCK_FACE_COLOR);*/

    // TODO Ticks

    // TODO Numbers
}

static void PaintHand(SDL_Renderer *renderer, const SDL_FPoint &center, const float radius, const float angleRadians, const float thickness, const uint32_t color)
{
    auto endPoint = CalculatePointFromCenter(center, radius, angleRadians);
    thickLineColor(renderer, center.x, center.y, endPoint.x, endPoint.y, thickness, color);
}

static void PaintHands(SDL_Renderer *renderer, const SDL_FPoint &center, const float radius, const std::chrono::hh_mm_ss<std::chrono::system_clock::duration> &time)
{
    // TODO Use seconds instead and calculate the hours, minutes and seconds from it
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
    filledCircleColor(renderer, center.x, center.y, radius * 0.04, CLOCK_SECOND_HAND_COLOR);
    filledCircleColor(renderer, center.x, center.y, radius * 0.02, CLOCK_MINUTE_HAND_COLOR);
}

AnalogClock::AnalogClock(const RendererPtr &renderer) : renderer(renderer)
{
    auto size = GetRendererSize(renderer);
    center = {size.width / 2.0f, size.height / 2.0f};
    radius = std::min(size.height, size.width) / 2 - 20;
}

void AnalogClock::Paint()
{
    using namespace std::chrono;
    auto now = system_clock::now();
    auto local = zoned_time{current_zone(), now}.get_local_time();
    auto today = floor<days>(local);
    auto timeSinceMidnight = local - today;
    hh_mm_ss hms{timeSinceMidnight};
    
    PaintClockFace(renderer.get(), center, radius);
    PaintHands(renderer.get(), center, radius, hms);
    SDL_RenderPresent(renderer.get());
}
