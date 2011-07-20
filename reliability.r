library(ggplot2)

reliability <- read.csv('/dev/stdin')
reliability$observed <- as.Date(reliability$observed, format="%Y-%m-%d %H:%M:%S")

Areas <- factor(reliability$area)
#Make the plot.
reliability_plot <- ggplot(reliability, aes(x=observed, y=outages))
reliability_plot <- reliability_plot + geom_area(aes(fill=Areas))
#Remove the grey padding.
reliability_plot <- reliability_plot + scale_x_date('Time', expand=c(0,0))
reliability_plot <- reliability_plot + scale_y_continuous('Customers Without Power', expand=c(0,0))

#Axis/title labeling
reliability_plot <- reliability_plot + opts(title='Pepco Reliability Statistics')
#reliability_plot <- reliability_plot + opts(legend.position=c(.5,.5))
#reliability_plot <- reliability_plot + scale_fill_manual()
reliability_dpi<-72
ggsave(reliability_plot,filename=commandArgs(trailingOnly=T)[1],
    width=900/reliability_dpi, height=150/reliability_dpi, dpi=reliability_dpi)
