#include <opencv2/opencv.hpp>
#include <string>

using namespace std;
using namespace cv;

void update_pixels(Mat src, float borderWidth, float radius);

int main() {
    for(int i = 0; i < 50; i++) {
        Mat tmp = Mat::zeros(Size(640,480), CV_8UC1);
        update_pixels(tmp, 100, (int) (300 - (i*5)));

        ostringstream ostr;
        ostr << i;

        imwrite(string("masks/torchMask_") + ostr.str() + ".png", tmp);
    }
}

void update_pixels(Mat src, float borderWidth, float radius) {

    for( int j = 0; j < src.rows; j++ ){ 
        for( int i = 0; i < src.cols; i++ ) {

            float distance = (float) sqrt((i-320)*(i-320)+(j-240)*(j-240));
            if(distance < radius) {
                if(distance > radius - borderWidth)
                    src.at<uchar>(j, i) = (int) ((1.0f-(distance - (radius - borderWidth))/borderWidth)*255);
                else
                    src.at<uchar>(j, i) = 255;
            }
        }
    }

    
}