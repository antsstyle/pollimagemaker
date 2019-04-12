# pollimagemaker
<h2>About PollImageMaker</h2>
PollImageMaker is a Java application for cropping and concatenating images, for Patreon polls or similar purposes. It allows you to put many images together, either as sets of two or as one image, adding numbers and text to show what each of your poll options are.

Here's an example of a finished image: https://i.imgur.com/9SsWYCK.png

<h2>How to use PollImageMaker</h2>

If you're an artist and want to use this, download the PollImageMaker-1.2.jar above and run it. You will need to have the Java Runtime Environment installed if you don't already, you can find that here: https://www.java.com/en/download/.

You can drag and drop images into the main window, at which the application will prompt you to crop the image if necessary. That window looks like this: (with whatever image you dragged in)

<img src="https://i.imgur.com/00DzJRA.png"></img>

The "dual-width image" option, if selected, causes the image to become one poll option on its own, which will span the entire width of the overall image as opposed to being one half of an option.

Upon adding one dual-width image, or adding two normal ones, the application will then prompt you to enter text describing this poll option (you can leave it blank for no text). You then continue adding images in this fashion until you're finished, at which point you can use the Save Image button on the left of the main panel.

The "Save Progress" button on the left allows you to save part-way through making your poll image, so in case for some reason you closed the application or something else, you can restore it to that point. You can also save a sub-image, which allows you to select only some of the options you put into the overall poll image.

The "Omit Numbers" button toggles the presence of numbers at the top left of each poll entry. Note that these will also be put into the finished image, along with the text, if they're enabled.

<h2>Trouble?</h2>

If something isn't working right or you encounter a bug, feel free to mention me on Twitter (<a href="https://twitter.com/antsstyle">@antsstyle</a>) and I can look into it.
