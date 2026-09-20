# Adin.lol

---

**adin.lol**

This was originally meant to be a base, just to see how well Fable / Opus could make a client,
and it came to be a bit more than I expected since I was having a ton of fun with it. So,
therefore, enjoy the client. I think it is a good client, and probably the better option when it
comes to skidding something, so I'll cut it short.

## Things worth skidding

- The rendering, able to render up to 5 million blocks with around a 10-20% fps decrease
- Backtrack / freeze
- Triggerbot
- Rotations

## Credits

- **timewarp** — inspiration for the GUI, as it is heavily based on theirs. I remade it from
  scratch, but better imo
- **xuc22 (me)** — making the client
- **Fable 5.1 / GPT-6 Astra / Opus 5** — pretty much coding everything
- **Graphics** — minor contributions and moral support

Keep in mind that this was made by an actual developer with real insight into how a client is
meant to be made and what a good client should look like. So no, this wasn't made in one prompt.
It was made over the course of around 2-3 weeks of working about 5+ hours a day.

And lastly, fuck everyone that sells vibecoded clients. Y'all don't deserve anything good. I
might've vibecoded this, but at least it's actually decent looking, decent performing, and open
source.

## Build steps

You need **JDK 25**. Everything else — Gradle 9.7.1, Minecraft 26.2, Fabric Loom — is pulled in
by the wrapper, so there is nothing else to install.

```bash
git clone https://github.com/kolfish/adin.git
cd adin
./gradlew build
```

On Windows use `gradlew.bat` instead of `./gradlew`.

The build drops `adin-1.0.0.jar` into `build/libs/`. The `-sources` jar beside it is just the
source and can be ignored.

To play with it, put that jar in your `.minecraft/mods/` folder along with:

- **Fabric Loader** 0.19.5 or newer
- **Fabric API** 0.159.0+26.2

To work on it instead, `./gradlew runClient` launches a dev client with the mod already loaded.
Press **Right Shift** in game to open the ClickGUI, or rebind it under
**Options → Controls → Key Binds → Adin**.

Thank you, and please enjoy :3
