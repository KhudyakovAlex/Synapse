# Процесс разработки

```mermaid
flowchart LR
    SBD((Совет<br>благородных<br>донов))
    
    ANAL((Анализ<br>Худяков))
    
    PRD(Техническое<br>задание)
    
    PRO((Проектир.<br>Худяков))
    
    DSUI(UI<br>Спецификация)
    DSUX(UX<br>Спецификация)
    DSAPP(Приложение<br>Спецификация)
    DSDB(База данных<br>Спецификация)
    DSP(USML<br>Спецификация)
    DSB(Bluetooth-соединение<br>Спецификация)
    DSLLM(Работа с LLM<br>Спецификация)
    DSFW(Прошивка<br>Спецификация)
    
    

    UIUXD((UX-дизайн<br>Худяков))
    UIUX( UX-макет )
    APPD(( Программ.<br>Булатов ))

    FWD(( Программ.<br>Саенко ))

    UIUID((UI-дизайн<br>Брянкина))
    UIUI( UI-макет )

    FW( Прошивка )
    APP( Приложение )

    SBD ---> ANAL
    ANAL ---> PRD
    PRD ---> PRO
    PRO ---> DSUI
    PRO ---> DSUX
    PRO ---> DSFW
    PRO ---> DSAPP
    PRO ---> DSP
    PRO ---> DSDB
    PRO ---> DSB
    PRO ---> DSLLM

    DSUX ---> UIUXD
    DSUI ---> UIUID
    UIUXD ---> UIUX
    UIUX ---> UIUID
    UIUID ---> UIUI

    UIUI ---> APPD
    DSAPP ---> APPD
    DSP ---> APPD
    DSDB ---> APPD
    DSB ---> APPD
    DSLLM ---> APPD

    DSFW ---> FWD
    DSP ---> FWD
    DSDB ---> FWD
    DSB ---> FWD

    FWD ---> FW
    APPD ---> APP

    style ANAL fill:#FFB3BA,stroke:#999
    style PRO fill:#BAFFC9,stroke:#999
    style UIUXD fill:#BAE1FF,stroke:#999
    style UIUID fill:#FFFFBA,stroke:#999
    style APPD fill:#BAF3FF,stroke:#999
    style FWD fill:#FFBAF3,stroke:#999
    
    style SBD fill:#505050,stroke:#999,color:#fff
    style PRD fill:#E0E0E0,stroke:#999
    style DSUI fill:#E0E0E0,stroke:#999
    style DSAPP fill:#E0E0E0,stroke:#999
    style DSDB fill:#E0E0E0,stroke:#999
    style DSP fill:#E0E0E0,stroke:#999
    style DSFW fill:#E0E0E0,stroke:#999
    style DSUX fill:#E0E0E0,stroke:#999
    style DSB fill:#E0E0E0,stroke:#999
    style DSLLM fill:#E0E0E0,stroke:#999
    style UIUX fill:#E0E0E0,stroke:#999
    style UIUI fill:#E0E0E0,stroke:#999
    style FW fill:#505050,stroke:#999,color:#fff
    style APP fill:#505050,stroke:#999,color:#fff

    click PRD "PRD/SynapsePRD.html"
    click DSUI "PDS/SynapsePDS_APP_UI.html"
    click DSUX "PDS/SynapsePDS_APP_UX.html"
    click DSAPP "PDS/SynapsePDS_APP.html"
    click DSDB "PDS/SynapsePDS_DB.html"
    click DSP "PDS/SynapsePDS_USML.html"
    click DSB "PDS/SynapsePDS_Bluetooth.html"
    click DSLLM "PDS/SynapsePDS_LLM.html"
    click DSFW "PDS/SynapsePDS_FW.html"

```