#ifndef __ALERT_SCREEN_HPP__
#define __ALERT_SCREEN_HPP__

#include "Alert.hpp"
#include "SDLHelpers.hpp"

class AlertScreen
{
public:
    AlertScreen(const RendererPtr &renderer);
    void Paint(const Alert &alert);

private:
    const RendererPtr &renderer;
    TextEnginePtr textEngine;
    FontPtr codeAndPrioFont;
    FontPtr assignmentDescriptionFont;
    FontPtr addressFont;
    FontPtr detailsFont;
    FontPtr unitsFont;
    FontPtr timerFont;
    Size size;
};

#endif