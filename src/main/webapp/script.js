// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/* eslint-disable no-unused-vars */

import {getSectionIdFromOption, showSection} from './menu-script.js';
import {signIn, loadClient} from './authentication.js';

/**
 * Shows a new slide of the slideshow identified by the classname of the
 * slides contained (all the slides of the same slideshow have the same class).
 * The new slide is obtained by adding numberOfSlides to the position of the
 * current slide in the slideshow.
 * @param {int} numberOfSlides The value that will be incremented to the
 * position of the current slide.
 * @param {String} slideClass The classname of the slides.
 */
function showSlide(numberOfSlides, slideClass) {
  const currentSlide = document.getElementById('current-' + slideClass);
  const slides = Array.from(document.getElementsByClassName(slideClass));
  const newSlide = slides[(slides.length + slides.indexOf(currentSlide) +
      numberOfSlides) % slides.length];

  currentSlide.id = '';
  currentSlide.style.display = 'none';

  newSlide.id = 'current-' + slideClass;
  newSlide.style.display = 'initial';
}

/**
 * Shows/hides a slideshow identified by its ID.
 * @param {String} slideshowId The ID associated with the slideshow.
 */
function showHideSlideshow(slideshowId) {
  const slideshow = document.getElementById(slideshowId);
  if (slideshow.classList.contains('current-slideshow')) {
    slideshow.classList.remove('current-slideshow');
    return;
  }

  const currentSlideshows = document.getElementsByClassName(
      'current-slideshow');
  for (const currentSlideshow of currentSlideshows) {
    currentSlideshow.classList.remove('current-slideshow');
  }
  slideshow.classList.add('current-slideshow');
}

window.showHideSlideshow = showHideSlideshow;
window.showSection = showSection;
window.showSlide = showSlide;
window.signIn = signIn;
window.addEventListener('load', loadClient);
